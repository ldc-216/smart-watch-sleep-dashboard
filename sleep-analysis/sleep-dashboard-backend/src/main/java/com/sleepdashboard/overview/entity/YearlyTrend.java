package com.sleepdashboard.overview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dws_yearly_trend")
public class YearlyTrend {
    @TableId
    private Integer yearRecorded;
    private Double avgSleepScore;
    private Double avgDurationMinutes;
    private Double avgEfficiencyPct;
    private Double insomniaRatioPct;
    private Long highApneaRiskCnt;
    private Long recordCnt;
}
