package com.sleepdashboard.correlation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_correlation_matrix")
public class CorrelationMatrix {
    @TableId
    private Long id;
    private String featureX;
    private String featureY;
    private Double corrValue;
}
