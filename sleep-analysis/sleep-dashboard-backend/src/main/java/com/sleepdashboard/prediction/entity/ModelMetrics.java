package com.sleepdashboard.prediction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_model_metrics")
public class ModelMetrics {
    @TableId
    private Long id;
    private String modelName;
    private Double rmse;
    private Double r2;
    private Integer featureCount;
}
