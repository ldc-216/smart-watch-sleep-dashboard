package com.sleepdashboard.prediction.dto;

import com.sleepdashboard.prediction.entity.ClusterProfile;
import com.sleepdashboard.prediction.entity.UserCluster;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * points: 每个用户的三维坐标+簇编号，喂给前端三维散点图直接渲染
 * profiles: 每个簇的均值特征+人工标签，前端用来生成图例/tooltip文字
 * （比如鼠标悬浮到某个簇的点上，显示"呼吸暂停高危型：平均血氧最低值 xx%"）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClusterResultDTO {
    private List<UserCluster> points;
    private List<ClusterProfile> profiles;
}
