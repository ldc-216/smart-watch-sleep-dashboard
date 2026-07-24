package com.sleepdashboard.profile.controller;

import com.sleepdashboard.auth.service.SysUserService;
import com.sleepdashboard.auth.util.UserContext;
import com.sleepdashboard.auth.dto.UserVO;
import com.sleepdashboard.common.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "个人睡眠画像接口")
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysUserService sysUserService;


    @GetMapping("/detail")
    public Result<Map<String, Object>> getProfileDetail(@org.springframework.web.bind.annotation.RequestParam(value = "targetUserId", required = false) String targetUserId) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }

        // 1. 获取登录用户名（即 sleep_data 中的 user_id）
        UserVO userVO = sysUserService.getLoginUserInfo(userId);
        String sleepUserId = userVO.getUsername();

        // 权限管理：仅允许 root 用户切换查看其他任意用户的画像
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            if ("root".equals(sleepUserId) || targetUserId.trim().equals(sleepUserId)) {
                sleepUserId = formatSleepUserId(targetUserId);
            }
        }

        // 校验目标用户在睡眠数据库中是否存在（root 管理员自身无数据，跳过校验）
        if (!"root".equals(sleepUserId)) {
            String checkSql = "SELECT COUNT(1) FROM dwd_sleep_detail WHERE user_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, sleepUserId);
            if (count == null || count == 0) {
                return Result.error(404, "未找到该用户的数据，请输入 1 ~ 20000 之间的用户 ID（如 user_00001 或直接输入数字 1）");
            }
        }

        Map<String, Object> response = new HashMap<>();
        response.put("userId", sleepUserId);

        // 2. 获取用户基本生物特征
        String baseSql = "SELECT age, gender, bmi, region, device_model FROM dwd_sleep_detail WHERE user_id = ? LIMIT 1";
        List<Map<String, Object>> baseList = jdbcTemplate.queryForList(baseSql, sleepUserId);
        if (baseList.isEmpty()) {
            // 如果明细表中没有数据，返回默认值
            response.put("age", 30);
            response.put("gender", "unknown");
            response.put("bmi", 22.0);
            response.put("region", "未知");
            response.put("deviceModel", "未知设备");
        } else {
            Map<String, Object> baseMap = baseList.get(0);
            response.put("age", baseMap.get("age"));
            response.put("gender", baseMap.get("gender"));
            response.put("bmi", baseMap.get("bmi"));
            response.put("region", baseMap.get("region"));
            response.put("deviceModel", baseMap.get("device_model"));
        }

        // 3. 聚合睡眠指标平均值
        String aggSql = "SELECT " +
                "  ROUND(AVG(sleep_score), 1) AS avgSleepScore, " +
                "  ROUND(AVG(sleep_duration_minutes) / 60.0, 1) AS avgSleepHours, " +
                "  ROUND(AVG(sleep_efficiency_pct), 1) AS avgSleepEfficiency, " +
                "  ROUND(AVG(sleep_latency_minutes), 1) AS avgSleepLatency, " +
                "  ROUND(AVG(spo2_min_pct), 1) AS avgSpo2Min, " +
                "  ROUND(AVG(snore_events), 1) AS avgSnoreEvents, " +
                "  ROUND(AVG(apnea_risk_score), 1) AS avgApneaRisk, " +
                "  ROUND(AVG(stress_score), 1) AS avgStressScore, " +
                "  ROUND(AVG(deep_sleep_pct), 1) AS avgDeepSleepPct, " +
                "  ROUND(AVG(light_sleep_pct), 1) AS avgLightSleepPct, " +
                "  ROUND(AVG(rem_sleep_pct), 1) AS avgRemSleepPct, " +
                "  ROUND(AVG(awake_pct), 1) AS avgAwakePct, " +
                "  ROUND(AVG(step_count_day), 0) AS avgStepCountDay, " +
                "  ROUND(AVG(caffeine_mg), 1) AS avgCaffeineMg, " +
                "  ROUND(AVG(screen_time_before_bed_min), 1) AS avgScreenTimeBeforeBedMin, " +
                "  ROUND(AVG(room_temperature_c), 1) AS avgRoomTemperatureC, " +
                "  ROUND(AVG(ambient_noise_db), 1) AS avgAmbientNoiseDb, " +
                "  ROUND(SUM(CASE WHEN medication_flag = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS medicationRatioPct " +
                "FROM dwd_sleep_detail " +
                "WHERE user_id = ?";
        List<Map<String, Object>> aggList = jdbcTemplate.queryForList(aggSql, sleepUserId);
        Map<String, Object> aggMap = new HashMap<>();
        if (!aggList.isEmpty()) {
            aggMap = aggList.get(0);
            System.out.println("DEBUG METRICS MAP FOR USER " + sleepUserId + ": " + aggMap);
        }
        response.put("metrics", aggMap);

        // 4. 获取最近 10 天的历史趋势
        String histSql = "SELECT date_recorded, sleep_score, sleep_efficiency_pct, " +
                "  ROUND(sleep_duration_minutes / 60.0, 1) AS sleepHours " +
                "FROM dwd_sleep_detail " +
                "WHERE user_id = ? " +
                "ORDER BY date_recorded DESC " +
                "LIMIT 10";
        List<Map<String, Object>> histList = jdbcTemplate.queryForList(histSql, sleepUserId);
        response.put("history", histList);

        // 5. 动态生成个性化画像报告与行为干预指导
        double score = aggMap.get("avgSleepScore") != null ? ((Number) aggMap.get("avgSleepScore")).doubleValue() : 75.0;
        double latency = aggMap.get("avgSleepLatency") != null ? ((Number) aggMap.get("avgSleepLatency")).doubleValue() : 15.0;
        double apnea = aggMap.get("avgApneaRisk") != null ? ((Number) aggMap.get("avgApneaRisk")).doubleValue() : 15.0;
        double spo2 = aggMap.get("avgSpo2Min") != null ? ((Number) aggMap.get("avgSpo2Min")).doubleValue() : 95.0;
        double stress = aggMap.get("avgStressScore") != null ? ((Number) aggMap.get("avgStressScore")).doubleValue() : 30.0;
        double deep = aggMap.get("avgDeepSleepPct") != null ? ((Number) aggMap.get("avgDeepSleepPct")).doubleValue() : 20.0;

        List<Map<String, String>> adviceList = new ArrayList<>();
        String personaTitle = "稳定睡眠状态";
        String personaDesc = "您的各项睡眠指标基本稳定。";

        // 得分判断
        if (score >= 85) {
            personaTitle = "卓越睡眠先锋";
            personaDesc = "您的睡眠得分非常优秀，核心深度睡眠充足，睡眠结构非常科学。";
        } else if (score >= 75) {
            personaTitle = "稳健型睡眠群体";
            personaDesc = "您的睡眠质量处于良好状态，但仍有轻微优化和改善空间。";
        } else {
            personaTitle = "亚健康睡眠负荷";
            personaDesc = "您的睡眠质量整体偏低，常伴有深度睡眠不足或日间精力欠佳。";
        }

        // 生成指导建议
        if (latency > 25.0) {
            Map<String, String> advice = new HashMap<>();
            advice.put("tag", "入睡障碍");
            advice.put("level", "warning");
            advice.put("title", "推行 CBT-I 睡眠限制疗法");
            advice.put("content", "您的平均入睡延迟达 " + latency + " 分钟。建议实行严格的作息，下午 15 点后严控咖啡因，且睡前 1 小时锁屏，缩短无序的卧床时间。");
            adviceList.add(advice);
        }

        if (apnea >= 30.0 || spo2 < 93.0) {
            Map<String, String> advice = new HashMap<>();
            advice.put("tag", "呼吸暂停高危");
            advice.put("level", "danger");
            advice.put("title", "调整夜间睡姿与临床转诊评估");
            advice.put("content", "您的呼吸暂停评分为 " + apnea + "，夜间最低血氧均值仅 " + spo2 + "%。强烈建议采用侧卧位阻断气道塌陷，严禁睡前饮酒。如日间持续疲倦，建议到呼吸科进行多导睡眠监测(PSG)。");
            adviceList.add(advice);
        }

        if (stress > 45.0) {
            Map<String, String> advice = new HashMap<>();
            advice.put("tag", "精神压力负荷");
            advice.put("level", "warning");
            advice.put("title", "睡前渐进式肌肉松弛（PMR）");
            advice.put("content", "您的日间压力指数偏高（平均 " + stress + "）。建议在睡前 20 分钟配合正念音频进行拉伸与深呼吸，平抑日间过度兴奋的交感神经。");
            adviceList.add(advice);
        }

        if (deep < 18.0) {
            Map<String, String> advice = new HashMap<>();
            advice.put("tag", "深睡眠缺失");
            advice.put("level", "info");
            advice.put("title", "核心体温调节调节建议");
            advice.put("content", "您的深睡眠占比（" + deep + "%）略低于标准水平。推荐将卧室夜间空调温度调低至 20℃，使机体核心温度顺利下降，从而加速诱发深睡眠。");
            adviceList.add(advice);
        }

        // 默认补充一条日常关怀
        if (adviceList.isEmpty()) {
            Map<String, String> advice = new HashMap<>();
            advice.put("tag", "日常关怀");
            advice.put("level", "success");
            advice.put("title", "保持现有的作息节律");
            advice.put("content", "您的各项生理指征表现优秀。继续坚持规律的就寝时间，睡前减少咖啡因与酒精依赖，是长期保持活力的基础。");
            adviceList.add(advice);
        }

        response.put("personaTitle", personaTitle);
        response.put("personaDesc", personaDesc);
        response.put("advices", adviceList);

        return Result.success(response);
    }

    @org.springframework.web.bind.annotation.PostMapping("/update")
    public Result<String> updateProfile(@org.springframework.web.bind.annotation.RequestBody Map<String, Object> req) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error(401, "未登录");
        }
        UserVO userVO = sysUserService.getLoginUserInfo(userId);
        String sleepUserId = userVO.getUsername();

        // 允许管理员指定目标用户ID更新其基本生物特征
        String targetUserId = (String) req.get("targetUserId");
        if (targetUserId != null && !targetUserId.trim().isEmpty()) {
            if ("root".equals(sleepUserId)) {
                sleepUserId = formatSleepUserId(targetUserId);
            }
        }

        int age = req.get("age") != null ? ((Number) req.get("age")).intValue() : 30;
        String gender = (String) req.get("gender");
        double bmi = req.get("bmi") != null ? ((Number) req.get("bmi")).doubleValue() : 22.0;
        String region = (String) req.get("region");
        String deviceModel = (String) req.get("deviceModel");

        String ageBucket = "30-45";
        if (age < 30) ageBucket = "18-30";
        else if (age >= 60) ageBucket = "60+";
        else if (age >= 45) ageBucket = "45-60";

        String updateSql = "UPDATE dwd_sleep_detail SET age = ?, age_bucket = ?, gender = ?, bmi = ?, region = ?, device_model = ? WHERE user_id = ?";
        jdbcTemplate.update(updateSql, age, ageBucket, gender, bmi, region, deviceModel, sleepUserId);

        return Result.success("修改成功");
    }

    private String formatSleepUserId(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase();
        if (s.isEmpty()) return s;
        
        // 如果全是数字，例如 "123" -> "user_00123"
        if (s.matches("\\d+")) {
            return String.format("user_%05d", Integer.parseInt(s));
        }
        
        // 如果是 "user_123" 或 "user123" -> "user_00123"
        if (s.startsWith("user")) {
            String numPart = s.substring(4).replace("_", "").trim();
            if (numPart.matches("\\d+")) {
                return String.format("user_%05d", Integer.parseInt(numPart));
            }
        }
        
        return s;
    }
}
