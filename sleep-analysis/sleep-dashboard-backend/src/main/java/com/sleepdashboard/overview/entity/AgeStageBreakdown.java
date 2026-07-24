package com.sleepdashboard.overview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dws_age_stage_breakdown")
public class AgeStageBreakdown {
    @TableId
    private String ageBucket;
    private Double avgDeepPct;
    private Double avgLightPct;
    private Double avgRemPct;
    private Double avgAwakePct;
}
