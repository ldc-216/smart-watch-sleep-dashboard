package com.sleepdashboard.prediction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_feature_importance")
public class FeatureImportance {
    @TableId
    private String featureName;
    private Double importance;
    private Double importancePct;
}
