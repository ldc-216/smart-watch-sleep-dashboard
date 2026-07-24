package com.sleepdashboard.overview.service;

import com.sleepdashboard.overview.dto.KpiCardDTO;
import com.sleepdashboard.overview.dto.Screen1OverviewDTO;
import com.sleepdashboard.overview.entity.YearlyTrend;
import com.sleepdashboard.overview.entity.MonthlyTrend;
import com.sleepdashboard.overview.entity.RegionSummary;
import com.sleepdashboard.overview.entity.DeviceShare;
import com.sleepdashboard.overview.entity.AgeStageBreakdown;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class Screen1Service {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Screen1OverviewDTO getOverview() {
        // 0. 直接查询全局排重后的 KPI 卡片统计值
        String kpiSql = "SELECT " +
                "  ROUND(AVG(sleep_score), 2) AS avgSleepScore, " +
                "  ROUND(AVG(sleep_duration_minutes), 2) AS avgDurationMinutes, " +
                "  ROUND(COUNT(DISTINCT CASE WHEN insomnia_flag = 1 THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2) AS insomniaRatioPct, " +
                "  COUNT(DISTINCT CASE WHEN apnea_risk_score >= 30 THEN user_id ELSE NULL END) AS highApneaRiskCnt, " +
                "  COUNT(*) AS totalRecordCnt " +
                "FROM dwd_sleep_detail";

        KpiCardDTO kpiCard = jdbcTemplate.queryForObject(kpiSql, (rs, rowNum) -> new KpiCardDTO(
                rs.getDouble("avgSleepScore"),
                rs.getDouble("avgDurationMinutes"),
                rs.getDouble("insomniaRatioPct"),
                rs.getLong("highApneaRiskCnt"),
                rs.getLong("totalRecordCnt")
        ));

        // 1. 查询年度趋势（动态从明细表聚合）
        String yearlySql = "SELECT " +
                "  year_recorded AS yearRecorded, " +
                "  ROUND(AVG(sleep_score), 2) AS avgSleepScore, " +
                "  ROUND(AVG(sleep_duration_minutes), 2) AS avgDurationMinutes, " +
                "  ROUND(AVG(sleep_efficiency_pct), 2) AS avgEfficiencyPct, " +
                "  ROUND(COUNT(DISTINCT CASE WHEN insomnia_flag = 1 THEN user_id ELSE NULL END) * 100.0 / COUNT(DISTINCT user_id), 2) AS insomniaRatioPct, " +
                "  COUNT(DISTINCT CASE WHEN apnea_risk_score >= 30 THEN user_id ELSE NULL END) AS highApneaRiskCnt, " +
                "  COUNT(*) AS recordCnt " +
                "FROM dwd_sleep_detail " +
                "GROUP BY year_recorded " +
                "ORDER BY year_recorded ASC";

        List<YearlyTrend> yearlyTrends = jdbcTemplate.query(yearlySql, (rs, rowNum) -> {
            YearlyTrend y = new YearlyTrend();
            y.setYearRecorded(rs.getInt("yearRecorded"));
            y.setAvgSleepScore(rs.getDouble("avgSleepScore"));
            y.setAvgDurationMinutes(rs.getDouble("avgDurationMinutes"));
            y.setAvgEfficiencyPct(rs.getDouble("avgEfficiencyPct"));
            y.setInsomniaRatioPct(rs.getDouble("insomniaRatioPct"));
            y.setHighApneaRiskCnt(rs.getLong("highApneaRiskCnt"));
            y.setRecordCnt(rs.getLong("recordCnt"));
            return y;
        });

        // 2. 查询月度趋势
        String monthlySql = "SELECT " +
                "  year_recorded AS yearRecorded, " +
                "  month_recorded AS monthRecorded, " +
                "  ROUND(AVG(sleep_score), 2) AS avgSleepScore, " +
                "  ROUND(AVG(sleep_efficiency_pct), 2) AS avgEfficiencyPct " +
                "FROM dwd_sleep_detail " +
                "GROUP BY year_recorded, month_recorded " +
                "ORDER BY year_recorded ASC, month_recorded ASC";

        List<MonthlyTrend> monthlyTrends = jdbcTemplate.query(monthlySql, (rs, rowNum) -> {
            MonthlyTrend m = new MonthlyTrend();
            m.setYearRecorded(rs.getInt("yearRecorded"));
            m.setMonthRecorded(rs.getInt("monthRecorded"));
            m.setAvgSleepScore(rs.getDouble("avgSleepScore"));
            m.setAvgEfficiencyPct(rs.getDouble("avgEfficiencyPct"));
            return m;
        });

        // 3. 查询地域分布
        String regionSql = "SELECT " +
                "  region, " +
                "  ROUND(AVG(sleep_duration_minutes), 2) AS avgDurationMinutes, " +
                "  ROUND(AVG(sleep_score), 2) AS avgSleepScore, " +
                "  COUNT(DISTINCT user_id) AS userCnt " +
                "FROM dwd_sleep_detail " +
                "GROUP BY region";

        List<RegionSummary> regionSummaries = jdbcTemplate.query(regionSql, (rs, rowNum) -> {
            RegionSummary r = new RegionSummary();
            r.setRegion(rs.getString("region"));
            r.setAvgDurationMinutes(rs.getDouble("avgDurationMinutes"));
            r.setAvgSleepScore(rs.getDouble("avgSleepScore"));
            r.setUserCnt(rs.getLong("userCnt"));
            return r;
        });

        // 4. 查询设备份额
        String deviceSql = "SELECT " +
                "  device_model AS deviceModel, " +
                "  COUNT(*) AS cnt, " +
                "  ROUND(COUNT(*) * 100.0 / (SELECT COUNT(*) FROM dwd_sleep_detail), 2) AS pct " +
                "FROM dwd_sleep_detail " +
                "GROUP BY device_model";

        List<DeviceShare> deviceShares = jdbcTemplate.query(deviceSql, (rs, rowNum) -> {
            DeviceShare d = new DeviceShare();
            d.setDeviceModel(rs.getString("deviceModel"));
            d.setCnt(rs.getLong("cnt"));
            d.setPct(rs.getDouble("pct"));
            return d;
        });

        // 5. 查询各年龄段阶段结构
        String ageSql = "SELECT " +
                "  age_bucket AS ageBucket, " +
                "  ROUND(AVG(deep_sleep_pct), 2) AS avgDeepPct, " +
                "  ROUND(AVG(light_sleep_pct), 2) AS avgLightPct, " +
                "  ROUND(AVG(rem_sleep_pct), 2) AS avgRemPct, " +
                "  ROUND(AVG(awake_pct), 2) AS avgAwakePct " +
                "FROM dwd_sleep_detail " +
                "GROUP BY age_bucket " +
                "ORDER BY age_bucket ASC";

        List<AgeStageBreakdown> ageStageBreakdowns = jdbcTemplate.query(ageSql, (rs, rowNum) -> {
            AgeStageBreakdown a = new AgeStageBreakdown();
            a.setAgeBucket(rs.getString("ageBucket"));
            a.setAvgDeepPct(rs.getDouble("avgDeepPct"));
            a.setAvgLightPct(rs.getDouble("avgLightPct"));
            a.setAvgRemPct(rs.getDouble("avgRemPct"));
            a.setAvgAwakePct(rs.getDouble("avgAwakePct"));
            return a;
        });

        return new Screen1OverviewDTO(
                kpiCard,
                yearlyTrends,
                monthlyTrends,
                regionSummaries,
                deviceShares,
                ageStageBreakdowns
        );
    }
}
