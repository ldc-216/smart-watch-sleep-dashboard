package com.sleepdashboard.prediction.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sleepdashboard.prediction.dto.ClusterResultDTO;
import com.sleepdashboard.prediction.entity.FeatureImportance;
import com.sleepdashboard.prediction.entity.ModelMetrics;
import com.sleepdashboard.prediction.mapper.ClusterProfileMapper;
import com.sleepdashboard.prediction.mapper.FeatureImportanceMapper;
import com.sleepdashboard.prediction.mapper.ModelMetricsMapper;
import com.sleepdashboard.prediction.mapper.UserClusterMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Screen4Service {

    @Autowired
    private FeatureImportanceMapper featureImportanceMapper;
    @Autowired
    private ModelMetricsMapper modelMetricsMapper;
    @Autowired
    private UserClusterMapper userClusterMapper;
    @Autowired
    private ClusterProfileMapper clusterProfileMapper;

    public List<FeatureImportance> getFeatureImportance() {
        QueryWrapper<FeatureImportance> qw = new QueryWrapper<>();
        qw.orderByDesc("importance");
        return featureImportanceMapper.selectList(qw);
    }

    public ModelMetrics getLatestModelMetrics() {
        QueryWrapper<ModelMetrics> qw = new QueryWrapper<>();
        qw.orderByDesc("updated_at").last("LIMIT 1");
        return modelMetricsMapper.selectOne(qw);
    }

    public ClusterResultDTO getClusterResult() {
        return new ClusterResultDTO(
                userClusterMapper.selectList(null),
                clusterProfileMapper.selectList(null)
        );
    }
}
