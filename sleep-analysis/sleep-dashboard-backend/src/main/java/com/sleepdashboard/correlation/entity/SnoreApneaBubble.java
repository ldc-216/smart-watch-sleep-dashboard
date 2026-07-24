package com.sleepdashboard.correlation.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("dws_snore_apnea_bubble")
public class SnoreApneaBubble {
    @TableId
    private Long id;
    private String userId;
    private Date dateRecorded;
    private Integer snoreEvents;
    private Double spo2MinPct;
    private Integer apneaRiskScore;
}
