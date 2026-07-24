package com.sleepdashboard.correlation.dto;

import com.sleepdashboard.correlation.entity.ActivityLatency;
import com.sleepdashboard.correlation.entity.CorrelationMatrix;
import com.sleepdashboard.correlation.entity.TemperatureEfficiency;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 屏3"生活与环境因素关联分析"一次性加载接口的返回结构，
 * 跟屏1的 Screen1OverviewDTO 是同一个思路：三张聚合表数据量都很小，
 * 一次返回，前端页面初始化时调一次接口即可。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Screen3OverviewDTO {
    private List<CorrelationMatrix> correlationMatrix;
    private List<TemperatureEfficiency> temperatureEfficiency;
    private List<ActivityLatency> activityLatency;
}
