package com.sleepdashboard.overview.dto;

import com.sleepdashboard.overview.entity.AgeStageBreakdown;
import com.sleepdashboard.overview.entity.DeviceShare;
import com.sleepdashboard.overview.entity.MonthlyTrend;
import com.sleepdashboard.overview.entity.RegionSummary;
import com.sleepdashboard.overview.entity.YearlyTrend;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 屏1"总体睡眠健康大屏"一次性加载接口的返回结构。
 * 前端页面初始化时调一次 /api/screen1/overview 就能拿到全部图表数据，
 * 减少多次请求的等待感（这几张表数据量都很小，一次性返回没有性能问题）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Screen1OverviewDTO {
    private KpiCardDTO kpi;
    private List<YearlyTrend> yearlyTrend;
    private List<MonthlyTrend> monthlyTrend;
    private List<RegionSummary> regionSummary;
    private List<DeviceShare> deviceShare;
    private List<AgeStageBreakdown> ageStageBreakdown;
}

