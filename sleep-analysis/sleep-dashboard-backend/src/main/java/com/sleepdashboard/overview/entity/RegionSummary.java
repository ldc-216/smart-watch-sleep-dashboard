package com.sleepdashboard.overview.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("dws_region_summary")
public class RegionSummary {
    @TableId
    private String region;
    private Double avgDurationMinutes;
    private Double avgSleepScore;
    private Long userCnt;
}
