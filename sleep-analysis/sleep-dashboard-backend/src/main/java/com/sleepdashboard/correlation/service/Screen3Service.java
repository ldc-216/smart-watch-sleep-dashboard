package com.sleepdashboard.correlation.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sleepdashboard.correlation.dto.Screen3OverviewDTO;
import com.sleepdashboard.correlation.entity.SnoreApneaBubble;
import com.sleepdashboard.correlation.mapper.ActivityLatencyMapper;
import com.sleepdashboard.correlation.mapper.CorrelationMatrixMapper;
import com.sleepdashboard.correlation.mapper.TemperatureEfficiencyMapper;
import com.sleepdashboard.correlation.mapper.SnoreApneaBubbleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Screen3Service {

    @Autowired
    private CorrelationMatrixMapper correlationMatrixMapper;
    @Autowired
    private TemperatureEfficiencyMapper temperatureEfficiencyMapper;
    @Autowired
    private ActivityLatencyMapper activityLatencyMapper;
    @Autowired
    private SnoreApneaBubbleMapper snoreApneaBubbleMapper;

    public Screen3OverviewDTO getOverview() {
        return new Screen3OverviewDTO(
                correlationMatrixMapper.selectList(null),
                temperatureEfficiencyMapper.selectList(null),
                activityLatencyMapper.selectList(null)
        );
    }

    public Page<SnoreApneaBubble> getSnoreApneaBubble(int page, int size) {
        return snoreApneaBubbleMapper.selectPage(new Page<>(page, size), null);
    }
}
