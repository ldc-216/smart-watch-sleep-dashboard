package com.sleepdashboard.prediction.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_cluster_profile")
public class ClusterProfile {
    @TableId
    private Integer clusterId;
    private Long userCnt;
    private Double avgSleepScore;
    private Double avgSleepEfficiencyPct;
    private Double avgHeartRateBpm;
    private Double avgHrvMs;
    private Double avgSpo2MinPct;
    private Double avgSnoreEvents;
    private Double avgApneaRiskScore;
    private Double avgStressScore;
    private Double insomniaRatio;
    private Double avgCaffeineMg;
    private Double avgAlcoholUnits;
    private Double avgScreenTimeBeforeBedMin;
    /** 人工根据以上均值判断后手动写入数据库的簇标签，比如"呼吸暂停高危型" */
    private String clusterLabel;
}
