package com.sleepdashboard.overview.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dws_monthly_trend")
public class MonthlyTrend {
    private Integer yearRecorded;
    private Integer monthRecorded;
    private Double avgSleepScore;
    private Double avgEfficiencyPct;
}
