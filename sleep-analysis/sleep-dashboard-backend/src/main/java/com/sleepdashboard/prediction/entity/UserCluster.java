package com.sleepdashboard.prediction.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("ads_user_cluster")
public class UserCluster {
    @TableId
    private String userId;
    private Integer clusterId;

    @TableField("pca_1")
    private Double pca1;

    @TableField("pca_2")
    private Double pca2;

    @TableField("pca_3")
    private Double pca3;
}
