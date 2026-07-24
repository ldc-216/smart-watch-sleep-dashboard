package com.sleepdashboard.prediction.service;

import com.sleepdashboard.prediction.dto.PredictRequest;
import com.sleepdashboard.prediction.dto.PredictResponse;
import lombok.extern.slf4j.Slf4j;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.EvaluatorUtil;
import org.jpmml.evaluator.FieldValue;
import org.jpmml.evaluator.InputField;
import org.jpmml.evaluator.TargetField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 加载 PmmlConfig 里注入的 Evaluator，做单条实时预测。
 * 注意：这里字段名要跟 Spark 训练脚本 05_ml_sleep_score_prediction.py 里
 * FEATURE_COLS 的列名（snake_case）严格一致，因为 jpmml-sparkml 导出 PMML 时
 * 用的就是 DataFrame 原始列名。
 *
 * 降级策略：当 PMML 文件尚未生成（Evaluator 为 null）时，
 * 使用简单线性规则估算分数，保证接口可用、系统能运行演示。
 */
@Slf4j
@Service
public class PredictionService {

    /**
     * 允许为 null——PMML 文件不存在时 PmmlConfig 返回 null bean，
     * 此处用 @Autowired(required = false) 接收。
     */
    @Autowired(required = false)
    private Evaluator sleepScoreEvaluator;

    private final Map<String, InputField> inputFieldMap = new java.util.concurrent.ConcurrentHashMap<>();
    private volatile TargetField targetField = null;
    private volatile boolean fieldsInitialized = false;

    private void initFields() {
        if (!fieldsInitialized && sleepScoreEvaluator != null) {
            synchronized (this) {
                if (!fieldsInitialized) {
                    for (InputField inputField : sleepScoreEvaluator.getInputFields()) {
                        inputFieldMap.put(inputField.getName(), inputField);
                    }
                    if (!sleepScoreEvaluator.getTargetFields().isEmpty()) {
                        targetField = sleepScoreEvaluator.getTargetFields().get(0);
                    }
                    fieldsInitialized = true;
                }
            }
        }
    }

    public PredictResponse predict(PredictRequest req) {
        if (sleepScoreEvaluator == null) {
            log.error("[PredictionService] PMML Evaluator 未加载，无法执行预测！");
            throw new IllegalStateException("机器学习预测模型未加载，请先同步PMML模型文件！");
        }
        return predictByPmml(req);
    }

    // -------------------------------------------------------------------------
    // PMML 模型预测（正式路径）
    // -------------------------------------------------------------------------

    private PredictResponse predictByPmml(PredictRequest req) {
        initFields();

        Map<String, FieldValue> arguments = new HashMap<>(16);
        arguments.put("step_count_day",              prepareField("step_count_day", req.getStepCountDay()));
        arguments.put("caffeine_mg",                 prepareField("caffeine_mg", req.getCaffeineMg()));
        arguments.put("alcohol_units",               prepareField("alcohol_units", req.getAlcoholUnits()));
        arguments.put("screen_time_before_bed_min",  prepareField("screen_time_before_bed_min", req.getScreenTimeBeforeBedMin()));
        arguments.put("stress_score",                prepareField("stress_score", req.getStressScore()));
        arguments.put("activity_before_bed_min",     prepareField("activity_before_bed_min", req.getActivityBeforeBedMin()));
        arguments.put("room_temperature_c",          prepareField("room_temperature_c", req.getRoomTemperatureC()));
        arguments.put("ambient_noise_db",            prepareField("ambient_noise_db", req.getAmbientNoiseDb()));
        arguments.put("bedtime_consistency_std_min", prepareField("bedtime_consistency_std_min", req.getBedtimeConsistencyStdMin()));
        arguments.put("nap_duration_minutes",        prepareField("nap_duration_minutes", req.getNapDurationMinutes()));
        arguments.put("age",                         prepareField("age", req.getAge()));
        arguments.put("bmi",                         prepareField("bmi", req.getBmi()));

        Map<String, ?> results = sleepScoreEvaluator.evaluate(arguments);

        String targetName = (targetField != null) ? targetField.getName() : "sleep_score";
        Object targetValue = results.get(targetName);
        Double predicted = (Double) EvaluatorUtil.decode(targetValue);

        // 保证预测值在 [0, 100] 合理区间内
        predicted = Math.min(100.0, Math.max(0.0, predicted));
        return new PredictResponse(Math.round(predicted * 100) / 100.0, "RandomForestRegressor");
    }

    private FieldValue prepareField(String name, Double value) {
        InputField field = inputFieldMap.get(name);
        if (field == null) {
            throw new IllegalArgumentException("缺少预测所需字段: " + name);
        }
        if (value == null) {
            throw new IllegalArgumentException("缺少预测所需字段值: " + name);
        }
        return field.prepare(value);
    }


}
