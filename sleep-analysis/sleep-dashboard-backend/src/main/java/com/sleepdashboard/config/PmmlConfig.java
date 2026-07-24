package com.sleepdashboard.config;

import lombok.extern.slf4j.Slf4j;
import org.jpmml.evaluator.Evaluator;
import org.jpmml.evaluator.LoadingModelEvaluatorBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.InputStream;

/**
 * 加载 Spark MLlib 训练完、用 jpmml-sparkml 导出的 PMML 模型文件。
 * 模型文件放在 src/main/resources/models/sleep_score_rf.pmml。
 *
 * 设计说明：
 *  - 使用 @Bean(name="sleepScoreEvaluator") 注入，PredictionService 用 @Autowired 获取。
 *  - 如果文件不存在（如首次启动、尚未跑完 Spark 训练）则返回 null，
 *    PredictionService 检查到 null 时返回一个基于规则的降级分数，
 *    避免整个 Spring Boot 因找不到 PMML 文件而无法启动。
 */
@Slf4j
@Configuration
public class PmmlConfig {

    @Value("${sleep-dashboard.ml.pmml-model-path}")
    private String pmmlModelPath;

    @Bean(name = "sleepScoreEvaluator")
    public Evaluator sleepScoreEvaluator() {
        try {
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource(pmmlModelPath);

            if (!resource.exists()) {
                log.warn("[PmmlConfig] PMML 模型文件不存在: {}，预测接口将使用规则降级模式。" +
                         "请先执行 05_ml_sleep_score_prediction.py 生成模型文件。", pmmlModelPath);
                return null;
            }

            try (InputStream is = resource.getInputStream()) {
                Evaluator evaluator = new LoadingModelEvaluatorBuilder()
                        .load(is)
                        .build();
                evaluator.verify(); // 用模型自带的样例数据校验一次，加载有问题会在启动时直接报错
                log.info("[PmmlConfig] PMML 模型加载成功: {}", pmmlModelPath);
                return evaluator;
            }
        } catch (Exception e) {
            log.warn("[PmmlConfig] PMML 模型加载失败，降级为规则模式。原因: {}", e.getMessage());
            return null;
        }
    }
}
