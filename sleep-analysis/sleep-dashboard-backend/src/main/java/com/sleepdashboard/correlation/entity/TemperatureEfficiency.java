package com.sleepdashboard.correlation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_temperature_efficiency")
public class TemperatureEfficiency {
    @TableId
    private String tempBucket;
    private Double avgEfficiencyPct;
    private Long recordCnt;
}
