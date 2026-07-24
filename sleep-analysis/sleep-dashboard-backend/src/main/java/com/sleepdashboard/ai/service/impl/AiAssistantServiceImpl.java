package com.sleepdashboard.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sleepdashboard.ai.service.AiAssistantService;
import com.sleepdashboard.auth.service.SysUserService;
import com.sleepdashboard.auth.util.UserContext;
import com.sleepdashboard.auth.dto.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiAssistantServiceImpl implements AiAssistantService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SysUserService sysUserService;

    @Value("${sleep-dashboard.ai.llm-url}")
    private String llmUrl;

    @Value("${sleep-dashboard.ai.llm-key}")
    private String llmKey;

    @Value("${sleep-dashboard.ai.model-name}")
    private String modelName;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String chat(String message) {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            return "未登录，请先登录系统。";
        }

        // 1. 获取登录用户名（即 sleep_data 中的 user_id）
        UserVO userVO = sysUserService.getLoginUserInfo(userId);
        String sleepUserId = userVO.getUsername();

        // 2. 调阅用户物理资料与生理指标
        String baseSql = "SELECT age, gender, bmi, region, device_model FROM dwd_sleep_detail WHERE user_id = ? LIMIT 1";
        List<Map<String, Object>> baseList = jdbcTemplate.queryForList(baseSql, sleepUserId);
        
        int age = 30;
        String gender = "unknown";
        double bmi = 22.0;
        String region = "未知";
        String deviceModel = "未知设备";
        
        if (!baseList.isEmpty()) {
            Map<String, Object> baseMap = baseList.get(0);
            if (baseMap.get("age") != null) age = ((Number) baseMap.get("age")).intValue();
            if (baseMap.get("gender") != null) gender = (String) baseMap.get("gender");
            if (baseMap.get("bmi") != null) bmi = ((Number) baseMap.get("bmi")).doubleValue();
            if (baseMap.get("region") != null) region = (String) baseMap.get("region");
            if (baseMap.get("device_model") != null) deviceModel = (String) baseMap.get("device_model");
        }

        // 3. 调阅用户指标平均值
        String aggSql = "SELECT " +
                "  AVG(sleep_score) AS avgScore, " +
                "  AVG(sleep_duration_minutes) AS avgDuration, " +
                "  AVG(sleep_efficiency_pct) AS avgEfficiency, " +
                "  AVG(sleep_latency_minutes) AS avgLatency, " +
                "  AVG(spo2_min_pct) AS avgSpo2, " +
                "  AVG(snore_events) AS avgSnore, " +
                "  AVG(apnea_risk_score) AS avgApnea, " +
                "  AVG(stress_score) AS avgStress, " +
                "  AVG(step_count_day) AS avgSteps, " +
                "  AVG(caffeine_mg) AS avgCaffeine, " +
                "  AVG(screen_time_before_bed_min) AS avgScreen, " +
                "  AVG(room_temperature_c) AS avgTemp, " +
                "  AVG(ambient_noise_db) AS avgNoise " +
                "FROM dwd_sleep_detail " +
                "WHERE user_id = ?";
        
        List<Map<String, Object>> aggList = jdbcTemplate.queryForList(aggSql, sleepUserId);
        
        double avgScore = 70.0;
        double avgHours = 7.0;
        double avgEfficiency = 90.0;
        double avgLatency = 20.0;
        double avgSpo2 = 96.0;
        double avgSnore = 2.0;
        double avgApnea = 10.0;
        double avgStress = 35.0;
        double avgSteps = 6000;
        double avgCaffeine = 50.0;
        double avgScreen = 60.0;
        double avgTemp = 22.0;
        double avgNoise = 35.0;

        if (!aggList.isEmpty() && aggList.get(0).get("avgScore") != null) {
            Map<String, Object> aggMap = aggList.get(0);
            avgScore = round(aggMap.get("avgScore"), 1);
            avgHours = round(((Number) aggMap.get("avgDuration")).doubleValue() / 60.0, 1);
            avgEfficiency = round(aggMap.get("avgEfficiency"), 1);
            avgLatency = round(aggMap.get("avgLatency"), 1);
            avgSpo2 = round(aggMap.get("avgSpo2"), 1);
            avgSnore = round(aggMap.get("avgSnore"), 1);
            avgApnea = round(aggMap.get("avgApnea"), 1);
            avgStress = round(aggMap.get("avgStress"), 1);
            avgSteps = round(aggMap.get("avgSteps"), 0);
            avgCaffeine = round(aggMap.get("avgCaffeine"), 1);
            avgScreen = round(aggMap.get("avgScreen"), 1);
            avgTemp = round(aggMap.get("avgTemp"), 1);
            avgNoise = round(aggMap.get("avgNoise"), 1);
        }

        // 4. 构建系统 System Prompt
        String systemPrompt = "你是一位专业的睡眠健康AI智能助手，拥有丰富的高级临床医学与行为干预（CBT-I）背景。\n" +
                "你正在为大屏网页用户解答问题。你已经获取了当前登录用户的个人健康监测背景数据：\n" +
                "用户名: " + sleepUserId + "\n" +
                "生理特征: 性别: " + (gender.equals("female") ? "女性" : "男性") + ", 年龄: " + age + "岁, BMI: " + bmi + ", 居住区域: " + region + ", 监测设备: " + deviceModel + "\n" +
                "最近监测期的生理及生活方式数据平均值如下:\n" +
                "- 睡眠质量: 平均得分 " + avgScore + " 分, 日均睡眠时长 " + avgHours + " 小时, 睡眠效率 " + avgEfficiency + "%\n" +
                "- 生理状态: 入睡潜伏期 " + avgLatency + " 分钟, 夜间最低血氧 " + avgSpo2 + "%, 每晚打鼾事件 " + avgSnore + " 次, 呼吸暂停风险指数 " + avgApnea + " 分, 日间平均压力值 " + avgStress + " 分\n" +
                "- 生活习性: 日间活动步数 " + (int)avgSteps + " 步, 咖啡因摄入量 " + avgCaffeine + " mg, 睡前屏幕暴露时长 " + avgScreen + " 分钟\n" +
                "- 卧室环境: 平均室内温度 " + avgTemp + " ℃, 环境噪音 " + avgNoise + " dB\n\n" +
                "【重要答复要求】\n" +
                "1. 在分析或回复时，必须且只能引用和结合该用户上述**真实的监测数据**，给出量化、个性化的解答，禁用无数据支撑的模棱两可或假设性推测。\n" +
                "2. 若用户提到关于睡眠差、打鼾多、环境温湿度或如何改善等，请直接以该数据为突破口。如打鼾多血氧低，提醒气道阻塞风险，给出侧卧等 CBT-I 指导；如睡前玩手机超标（合理为30m内），指出蓝光对褪黑素的影响；若一切正常，请表达日常的关怀和鼓励。\n" +
                "3. 回答要有专业医学底蕴，同时排版利落清爽，段落清晰，使用规范的中文字符和 Markdown 格式。字数严格控制在 350 字以内，字字珠玑，切忌冗长罗嗦。";

        // 5. 组装 DeepSeek API 请求体
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(llmKey);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", modelName);

            List<Map<String, String>> messages = new ArrayList<>();
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemPrompt);
            messages.add(sysMsg);

            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", message);
            messages.add(userMsg);

            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.6);
            requestBody.put("max_tokens", 800);

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(llmUrl, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode choices = rootNode.path("choices");
                if (choices.isArray() && choices.size() > 0) {
                    return choices.get(0).path("message").path("content").asText().trim();
                }
            }
            return "AI 助手暂时无法生成回复，请稍后再试。";
        } catch (Exception e) {
            System.err.println("[AI-API-ERROR] Calling DeepSeek failed: " + e.getMessage());
            return "AI 模块连接异常，错误信息: " + e.getMessage();
        }
    }

    private double round(Object obj, int decimals) {
        if (obj == null) return 0.0;
        double val = ((Number) obj).doubleValue();
        double factor = Math.pow(10, decimals);
        return Math.round(val * factor) / factor;
    }
}
