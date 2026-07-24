package com.sleepdashboard.correlation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_activity_latency")
public class ActivityLatency {
    @TableId
    private String activityGroup;
    private Double avgSleepLatencyMinutes;
    private Long recordCnt;
}
