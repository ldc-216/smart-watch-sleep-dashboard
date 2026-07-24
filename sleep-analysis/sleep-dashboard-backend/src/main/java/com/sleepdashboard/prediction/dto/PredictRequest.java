package com.sleepdashboard.prediction.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 睡眠得分预测请求参数。
 * 字段顺序和命名对应 Spark 训练脚本 05_ml_sleep_score_prediction.py 里的 FEATURE_COLS，
 * 前端"预测模拟器"的每个滑块/输入框对应下面一个字段。
 * 如果以后调整了训练特征，这里也要同步改，两边必须严格一致，
 * 否则 PMML 预测时字段对不上会直接报错。
 */
@Data
public class PredictRequest {

    @NotNull
    private Double stepCountDay;              // 当日步数

    @NotNull
    private Double caffeineMg;                // 咖啡因摄入量(mg)

    @NotNull
    private Double alcoholUnits;              // 酒精摄入量

    @NotNull
    private Double screenTimeBeforeBedMin;    // 睡前屏幕时间(分钟)

    @NotNull
    private Double stressScore;               // 压力指数评分

    @NotNull
    private Double activityBeforeBedMin;      // 睡前运动时长(分钟)

    @NotNull
    private Double roomTemperatureC;          // 房间温度

    @NotNull
    private Double ambientNoiseDb;            // 环境噪音

    @NotNull
    private Double bedtimeConsistencyStdMin;  // 就寝时间一致性标准差

    @NotNull
    private Double napDurationMinutes;        // 白天小睡时长

    @NotNull
    private Double age;                       // 年龄

    @NotNull
    private Double bmi;                       // BMI
}
