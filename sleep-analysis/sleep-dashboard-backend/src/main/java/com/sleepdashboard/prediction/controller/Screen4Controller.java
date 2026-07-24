package com.sleepdashboard.prediction.controller;

import com.sleepdashboard.common.Result;
import com.sleepdashboard.prediction.dto.ClusterResultDTO;
import com.sleepdashboard.prediction.dto.PredictRequest;
import com.sleepdashboard.prediction.dto.PredictResponse;
import com.sleepdashboard.prediction.entity.FeatureImportance;
import com.sleepdashboard.prediction.entity.ModelMetrics;
import com.sleepdashboard.prediction.service.PredictionService;
import com.sleepdashboard.prediction.service.Screen4Service;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "屏4-智能预测与诊断预警")
@RestController
@RequestMapping("/api/screen4")
public class Screen4Controller {

    @Autowired
    private PredictionService predictionService;
    @Autowired
    private Screen4Service screen4Service;

    /**
     * 睡眠得分预测模拟器：前端把滑块/输入框的值传进来，
     * 走 PMML 纯内存计算，毫秒级返回，不依赖 Spark。
     */
    @PostMapping("/predict")
    public Result<PredictResponse> predict(@Validated @RequestBody PredictRequest request) {
        return Result.success(predictionService.predict(request));
    }

    /** 特征重要性排名条形图 */
    @GetMapping("/feature-importance")
    public Result<List<FeatureImportance>> featureImportance() {
        return Result.success(screen4Service.getFeatureImportance());
    }

    /** 模型评估指标，可以展示在预测模拟器旁边，告诉用户模型精度 */
    @GetMapping("/model-metrics")
    public Result<ModelMetrics> modelMetrics() {
        return Result.success(screen4Service.getLatestModelMetrics());
    }

    /** 人群聚类三维散点图数据 + 每个簇的特征均值(用于图例/tooltip) */
    @GetMapping("/cluster")
    public Result<ClusterResultDTO> cluster() {
        return Result.success(screen4Service.getClusterResult());
    }
}
