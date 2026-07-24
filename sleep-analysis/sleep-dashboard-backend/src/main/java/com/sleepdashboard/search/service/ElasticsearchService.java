package com.sleepdashboard.search.service;

import com.sleepdashboard.search.dto.SearchRequest;
import com.sleepdashboard.search.dto.SearchResultDTO;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.ParsedAvg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {

    private static final String INDEX = "sleep_records";

    @Autowired
    private RestHighLevelClient client;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    /**
     * 多条件搜索：把 SearchRequest 里非空的字段动态拼成 bool filter。
     * 这是"百度式搜索体验"的核心——用户勾选什么条件就加什么 filter，
     * 不需要针对每种组合写死查询。
     * 
     * 聚合分析扩展：利用 Elasticsearch 的高并发聚合引擎，在用户检索的同时，
     * 实时统计出当前搜索结果子集下的：性别比例、平均睡眠得分、平均睡眠效率、
     * 平均心率、平均压力指数、平均咖啡因摄入、平均酒精单位及平均睡前屏幕时长。
     */
    public SearchResultDTO search(SearchRequest req) throws Exception {
        BoolQueryBuilder bool = QueryBuilders.boolQuery();

        if (req.getUserId() != null && !req.getUserId().trim().isEmpty()) {
            bool.filter(QueryBuilders.termQuery("user_id", req.getUserId().trim()));
        }
        if (req.getGender() != null && !req.getGender().trim().isEmpty()) {
            bool.filter(QueryBuilders.termQuery("gender", req.getGender().trim()));
        }
        if (req.getMedicationFlag() != null) {
            bool.filter(QueryBuilders.termQuery("medication_flag", req.getMedicationFlag()));
        }
        if (req.getInsomniaFlag() != null) {
            bool.filter(QueryBuilders.termQuery("insomnia_flag", req.getInsomniaFlag()));
        }
        if (req.getRegion() != null && !req.getRegion().trim().isEmpty()) {
            bool.filter(QueryBuilders.termQuery("region", req.getRegion().trim()));
        }
        if (req.getDeviceModel() != null && !req.getDeviceModel().trim().isEmpty()) {
            bool.filter(QueryBuilders.termQuery("device_model", req.getDeviceModel().trim()));
        }
        if (req.getAgeMin() != null || req.getAgeMax() != null) {
            org.elasticsearch.index.query.RangeQueryBuilder ageRange = QueryBuilders.rangeQuery("age");
            if (req.getAgeMin() != null) ageRange.gte(req.getAgeMin());
            if (req.getAgeMax() != null) ageRange.lte(req.getAgeMax());
            bool.filter(ageRange);
        }
        if (req.getSleepScoreMin() != null || req.getSleepScoreMax() != null) {
            org.elasticsearch.index.query.RangeQueryBuilder scoreRange = QueryBuilders.rangeQuery("sleep_score");
            if (req.getSleepScoreMin() != null) scoreRange.gte(req.getSleepScoreMin());
            if (req.getSleepScoreMax() != null) scoreRange.lte(req.getSleepScoreMax());
            bool.filter(scoreRange);
        }
        if (req.getSnoreEventsMin() != null || req.getSnoreEventsMax() != null) {
            org.elasticsearch.index.query.RangeQueryBuilder snoreRange = QueryBuilders.rangeQuery("snore_events");
            if (req.getSnoreEventsMin() != null) snoreRange.gte(req.getSnoreEventsMin());
            if (req.getSnoreEventsMax() != null) snoreRange.lte(req.getSnoreEventsMax());
            bool.filter(snoreRange);
        }
        if (req.getBmiMin() != null || req.getBmiMax() != null) {
            org.elasticsearch.index.query.RangeQueryBuilder bmiRange = QueryBuilders.rangeQuery("bmi");
            if (req.getBmiMin() != null) bmiRange.gte(req.getBmiMin());
            if (req.getBmiMax() != null) bmiRange.lte(req.getBmiMax());
            bool.filter(bmiRange);
        }

        String sortField = "date_recorded";
        SortOrder sortOrder = SortOrder.DESC;

        if (req.getSortField() != null && !req.getSortField().trim().isEmpty()) {
            String field = req.getSortField().trim();
            if ("userId".equals(field)) sortField = "user_id";
            else if ("dateRecorded".equals(field)) sortField = "date_recorded";
            else if ("age".equals(field)) sortField = "age";
            else if ("bmi".equals(field)) sortField = "bmi";
            else if ("sleepScore".equals(field)) sortField = "sleep_score";
            else if ("apneaRiskScore".equals(field)) sortField = "apnea_risk_score";
            else if ("stressScore".equals(field)) sortField = "stress_score";
        }

        if (req.getSortOrder() != null && "asc".equalsIgnoreCase(req.getSortOrder().trim())) {
            sortOrder = SortOrder.ASC;
        }

        SearchSourceBuilder source = new SearchSourceBuilder()
                .query(bool)
                .sort(sortField, sortOrder)
                .from((req.getPage() - 1) * req.getSize())
                .size(req.getSize())
                .trackTotalHits(true); // 追踪精确文档数，突破默认的 10000 条限制

        // 注入 Elasticsearch 实时聚合统计，实时生成当前检索结果集的人群画像
        source.aggregation(AggregationBuilders.avg("avg_sleep_score").field("sleep_score"));
        source.aggregation(AggregationBuilders.avg("avg_efficiency").field("sleep_efficiency_pct"));
        source.aggregation(AggregationBuilders.avg("avg_heart_rate").field("heart_rate_mean_bpm"));
        source.aggregation(AggregationBuilders.avg("avg_stress").field("stress_score"));
        source.aggregation(AggregationBuilders.avg("avg_caffeine").field("caffeine_mg"));
        source.aggregation(AggregationBuilders.avg("avg_alcohol").field("alcohol_units"));
        source.aggregation(AggregationBuilders.avg("avg_screen_time").field("screen_time_before_bed_min"));
        source.aggregation(AggregationBuilders.terms("gender_ratio").field("gender"));
        source.aggregation(AggregationBuilders.terms("region_ratio").field("region").size(50));

        org.elasticsearch.action.search.SearchRequest esRequest =
                new org.elasticsearch.action.search.SearchRequest(INDEX).source(source);
        SearchResponse response = client.search(esRequest, RequestOptions.DEFAULT);

        // 解析聚合结果
        Map<String, Object> stats = new HashMap<>();
        long totalHits = response.getHits().getTotalHits().value;
        if (totalHits > 0 && response.getAggregations() != null) {
            ParsedAvg avgSleep = response.getAggregations().get("avg_sleep_score");
            ParsedAvg avgEff = response.getAggregations().get("avg_efficiency");
            ParsedAvg avgHr = response.getAggregations().get("avg_heart_rate");
            ParsedAvg avgStress = response.getAggregations().get("avg_stress");
            ParsedAvg avgCaf = response.getAggregations().get("avg_caffeine");
            ParsedAvg avgAlc = response.getAggregations().get("avg_alcohol");
            ParsedAvg avgScr = response.getAggregations().get("avg_screen_time");
            ParsedStringTerms genderTerms = response.getAggregations().get("gender_ratio");
            ParsedStringTerms regionTerms = response.getAggregations().get("region_ratio");

            stats.put("avgSleepScore", isInvalidDouble(avgSleep.getValue()) ? 0.0 : Math.round(avgSleep.getValue() * 100.0) / 100.0);
            stats.put("avgEfficiency", isInvalidDouble(avgEff.getValue()) ? 0.0 : Math.round(avgEff.getValue() * 100.0) / 100.0);
            stats.put("avgHeartRate", isInvalidDouble(avgHr.getValue()) ? 0.0 : Math.round(avgHr.getValue() * 100.0) / 100.0);
            stats.put("avgStress", isInvalidDouble(avgStress.getValue()) ? 0.0 : Math.round(avgStress.getValue() * 100.0) / 100.0);
            stats.put("avgCaffeine", isInvalidDouble(avgCaf.getValue()) ? 0.0 : Math.round(avgCaf.getValue() * 100.0) / 100.0);
            stats.put("avgAlcohol", isInvalidDouble(avgAlc.getValue()) ? 0.0 : Math.round(avgAlc.getValue() * 100.0) / 100.0);
            stats.put("avgScreenTime", isInvalidDouble(avgScr.getValue()) ? 0.0 : Math.round(avgScr.getValue() * 100.0) / 100.0);

            stats.put("genderRatio", getGenderRatioFromMySql(req));

            stats.put("regionRatio", getRegionRatioFromMySql(req));
        } else {
            stats.put("avgSleepScore", 0.0);
            stats.put("avgEfficiency", 0.0);
            stats.put("avgHeartRate", 0.0);
            stats.put("avgStress", 0.0);
            stats.put("avgCaffeine", 0.0);
            stats.put("avgAlcohol", 0.0);
            stats.put("avgScreenTime", 0.0);
            stats.put("genderRatio", new HashMap<String, Long>());
            stats.put("regionRatio", new HashMap<String, Long>());
        }

        // 把匹配文档转为 Map 列表
        List<Map<String, Object>> records = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            records.add(hit.getSourceAsMap());
        }

        return new SearchResultDTO(response.getHits().getTotalHits().value, records, stats);
    }

    private SearchResultDTO toResultDTO(SearchResponse response) {
        List<Map<String, Object>> records = new ArrayList<>();
        for (SearchHit hit : response.getHits().getHits()) {
            records.add(hit.getSourceAsMap());
        }
        return new SearchResultDTO(response.getHits().getTotalHits().value, records);
    }

    /**
     * 批量导入数据到 Elasticsearch Index
     */
    public void importRecords(List<Map<String, Object>> records) throws Exception {
        if (records == null || records.isEmpty()) {
            return;
        }

        // 1. 在真正写入之前，对所有记录进行预校验，避免“部分成功”的脏数据
        for (Map<String, Object> record : records) {
            String userId = (String) record.get("user_id");
            String dateRecorded = (String) record.get("date_recorded");
            if (userId == null || dateRecorded == null) {
                throw new IllegalArgumentException("user_id 和 date_recorded 不能为空！");
            }
            userId = userId.trim();
            dateRecorded = dateRecorded.trim();

            // 查询该用户在 MySQL 中是否存在
            String checkUserSql = "SELECT age, gender, region, device_model FROM dwd_sleep_detail WHERE user_id = ? LIMIT 1";
            List<Map<String, Object>> existingUserList = jdbcTemplate.queryForList(checkUserSql, userId);

            if (!existingUserList.isEmpty()) {
                // 情形 A：原有用户
                Map<String, Object> existingUser = existingUserList.get(0);
                int existingAge = ((Number) existingUser.get("age")).intValue();
                String existingGender = (String) existingUser.get("gender");
                String existingRegion = (String) existingUser.get("region");
                String existingDevice = (String) existingUser.get("device_model");

                // 校验日期重复性（不能插入已有的日期）
                String checkDateSql = "SELECT COUNT(1) FROM dwd_sleep_detail WHERE user_id = ? AND date_recorded = ?";
                Integer dateCount = jdbcTemplate.queryForObject(checkDateSql, Integer.class, userId, dateRecorded);
                if (dateCount != null && dateCount > 0) {
                    throw new IllegalArgumentException("用户 " + userId + " 在日期 " + dateRecorded + " 已存在监测记录，不能重复录入！");
                }

                // 校验基本属性一致性并填充缺失字段
                if (record.containsKey("gender") && record.get("gender") != null) {
                    String inputGender = ((String) record.get("gender")).trim();
                    if (!inputGender.equalsIgnoreCase(existingGender)) {
                        throw new IllegalArgumentException("原有用户 " + userId + " 的性别为 " + existingGender + "，不能修改为 " + inputGender);
                    }
                } else {
                    record.put("gender", existingGender);
                }

                if (record.containsKey("age") && record.get("age") != null) {
                    int inputAge = ((Number) record.get("age")).intValue();
                    if (inputAge != existingAge) {
                        throw new IllegalArgumentException("原有用户 " + userId + " 的年龄为 " + existingAge + "，不能修改为 " + inputAge);
                    }
                } else {
                    record.put("age", existingAge);
                }

                if (record.containsKey("region") && record.get("region") != null) {
                    String inputRegion = ((String) record.get("region")).trim();
                    if (!inputRegion.equalsIgnoreCase(existingRegion)) {
                        throw new IllegalArgumentException("原有用户 " + userId + " 的地区为 " + existingRegion + "，不能修改为 " + inputRegion);
                    }
                } else {
                    record.put("region", existingRegion);
                }

                if (record.containsKey("device_model") && record.get("device_model") != null) {
                    String inputDevice = ((String) record.get("device_model")).trim();
                    if (!inputDevice.equalsIgnoreCase(existingDevice)) {
                        throw new IllegalArgumentException("原有用户 " + userId + " 的设备为 " + existingDevice + "，不能修改为 " + inputDevice);
                    }
                } else {
                    record.put("device_model", existingDevice);
                }
            } else {
                // 情形 B：完全新用户
                if (record.get("gender") == null || record.get("age") == null || record.get("region") == null || record.get("device_model") == null) {
                    throw new IllegalArgumentException("导入全新用户 " + userId + " 的首条数据时，必须提供完整的性别、年龄、地区和设备型号！");
                }
            }
        }

        // 2. 校验通过，执行批量持久化
        org.elasticsearch.action.bulk.BulkRequest bulkRequest = new org.elasticsearch.action.bulk.BulkRequest();
        for (Map<String, Object> record : records) {
            String userId = ((String) record.get("user_id")).trim();
            String dateRecorded = ((String) record.get("date_recorded")).trim();
            String docId = userId + "_" + dateRecorded;
            org.elasticsearch.action.index.IndexRequest indexRequest = new org.elasticsearch.action.index.IndexRequest(INDEX)
                    .id(docId)
                    .source(record);
            bulkRequest.add(indexRequest);

            // MySQL 同步插入
            jdbcTemplate.update("DELETE FROM dwd_sleep_detail WHERE user_id = ? AND date_recorded = ?", userId, dateRecorded);
            insertIntoMySql(record);
        }
        if (bulkRequest.numberOfActions() > 0) {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 删除单条数据记录
     */
    public void deleteRecord(String userId, String dateRecorded) throws Exception {
        String docId = userId.trim() + "_" + dateRecorded.trim();
        org.elasticsearch.action.delete.DeleteRequest deleteRequest =
                new org.elasticsearch.action.delete.DeleteRequest(INDEX, docId);
        client.delete(deleteRequest, RequestOptions.DEFAULT);

        // 同步从 MySQL 删除明细数据
        jdbcTemplate.update("DELETE FROM dwd_sleep_detail WHERE user_id = ? AND date_recorded = ?", userId.trim(), dateRecorded.trim());
    }

    /**
     * 批量删除数据记录
     */
    public void deleteRecords(java.util.List<java.util.Map<String, String>> keys) throws Exception {
        org.elasticsearch.action.bulk.BulkRequest bulkRequest = new org.elasticsearch.action.bulk.BulkRequest();
        for (java.util.Map<String, String> key : keys) {
            String userId = key.get("userId");
            String dateRecorded = key.get("dateRecorded");
            if (userId != null && dateRecorded != null) {
                String docId = userId.trim() + "_" + dateRecorded.trim();
                bulkRequest.add(new org.elasticsearch.action.delete.DeleteRequest(INDEX, docId));
                jdbcTemplate.update("DELETE FROM dwd_sleep_detail WHERE user_id = ? AND date_recorded = ?", userId.trim(), dateRecorded.trim());
            }
        }
        if (bulkRequest.numberOfActions() > 0) {
            client.bulk(bulkRequest, RequestOptions.DEFAULT);
        }
    }

    /**
     * 更新单条数据记录
     */
    public void updateRecord(Map<String, Object> record) throws Exception {
        String userId = (String) record.get("user_id");
        String dateRecorded = (String) record.get("date_recorded");
        if (userId == null || dateRecorded == null) {
            throw new IllegalArgumentException("user_id and date_recorded must not be null");
        }
        String docId = userId.trim() + "_" + dateRecorded.trim();
        org.elasticsearch.action.index.IndexRequest indexRequest = new org.elasticsearch.action.index.IndexRequest(INDEX)
                .id(docId)
                .source(record);
        client.index(indexRequest, RequestOptions.DEFAULT);

        // 同步覆盖更新 MySQL 明细数据
        jdbcTemplate.update("DELETE FROM dwd_sleep_detail WHERE user_id = ? AND date_recorded = ?", userId.trim(), dateRecorded.trim());
        insertIntoMySql(record);
    }

    /**
     * 辅助方法：向 MySQL dwd_sleep_detail 插入一条新记录，处理默认值及衍生字段
     */
    private void insertIntoMySql(Map<String, Object> record) {
        String userId = (String) record.get("user_id");
        String dateRecorded = (String) record.get("date_recorded");
        
        int yearRecorded = 2026;
        int monthRecorded = 7;
        try {
            String[] parts = dateRecorded.split("-");
            if (parts.length >= 2) {
                yearRecorded = Integer.parseInt(parts[0]);
                monthRecorded = Integer.parseInt(parts[1]);
            }
        } catch (Exception ignored) {}

        int age = record.get("age") != null ? ((Number) record.get("age")).intValue() : 35;
        
        String ageBucket = "30-45";
        if (age < 30) ageBucket = "18-30";
        else if (age >= 60) ageBucket = "60+";
        else if (age >= 45) ageBucket = "45-60";

        String gender = (String) record.get("gender");
        double bmi = record.get("bmi") != null ? ((Number) record.get("bmi")).doubleValue() : 24.2;
        String region = (String) record.get("region");
        String deviceModel = (String) record.get("device_model");
        double duration = record.get("sleep_duration_minutes") != null ? ((Number) record.get("sleep_duration_minutes")).doubleValue() : 480.0;
        int score = record.get("sleep_score") != null ? ((Number) record.get("sleep_score")).intValue() : 75;
        double efficiency = record.get("sleep_efficiency_pct") != null ? ((Number) record.get("sleep_efficiency_pct")).doubleValue() : 90.0;
        double latency = record.get("sleep_latency_minutes") != null ? ((Number) record.get("sleep_latency_minutes")).doubleValue() : 15.0;
        double spo2Min = record.get("spo2_min_pct") != null ? ((Number) record.get("spo2_min_pct")).doubleValue() : 95.0;
        int snore = record.get("snore_events") != null ? ((Number) record.get("snore_events")).intValue() : 0;
        int apnea = record.get("apnea_risk_score") != null ? ((Number) record.get("apnea_risk_score")).intValue() : 15;
        int insomnia = record.get("insomnia_flag") != null ? ((Number) record.get("insomnia_flag")).intValue() : 0;
        int stress = record.get("stress_score") != null ? ((Number) record.get("stress_score")).intValue() : 30;
        int medication = record.get("medication_flag") != null ? ((Number) record.get("medication_flag")).intValue() : 0;

        double deep = record.get("deep_sleep_pct") != null ? ((Number) record.get("deep_sleep_pct")).doubleValue() : 20.0;
        double light = record.get("light_sleep_pct") != null ? ((Number) record.get("light_sleep_pct")).doubleValue() : 50.0;
        double rem = record.get("rem_sleep_pct") != null ? ((Number) record.get("rem_sleep_pct")).doubleValue() : 20.0;
        double awake = record.get("awake_pct") != null ? ((Number) record.get("awake_pct")).doubleValue() : 10.0;

        int hr = record.get("heart_rate_mean_bpm") != null ? ((Number) record.get("heart_rate_mean_bpm")).intValue() : 60;
        int steps = record.get("step_count_day") != null ? ((Number) record.get("step_count_day")).intValue() : 8000;
        double caffeine = record.get("caffeine_mg") != null ? ((Number) record.get("caffeine_mg")).doubleValue() : 0.0;
        double screen = record.get("screen_time_before_bed_min") != null ? ((Number) record.get("screen_time_before_bed_min")).doubleValue() : 0.0;
        double temp = record.get("room_temperature_c") != null ? ((Number) record.get("room_temperature_c")).doubleValue() : 21.0;
        double noise = record.get("ambient_noise_db") != null ? ((Number) record.get("ambient_noise_db")).doubleValue() : 30.0;

        String sql = "INSERT INTO dwd_sleep_detail (" +
                "  user_id, date_recorded, year_recorded, month_recorded, age, age_bucket, " +
                "  gender, bmi, region, device_model, sleep_duration_minutes, sleep_score, " +
                "  sleep_efficiency_pct, sleep_latency_minutes, spo2_min_pct, snore_events, " +
                "  apnea_risk_score, insomnia_flag, stress_score, medication_flag, " +
                "  deep_sleep_pct, light_sleep_pct, rem_sleep_pct, awake_pct, " +
                "  heart_rate_mean_bpm, step_count_day, caffeine_mg, screen_time_before_bed_min, " +
                "  room_temperature_c, ambient_noise_db" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                userId, dateRecorded, yearRecorded, monthRecorded, age, ageBucket,
                gender, bmi, region, deviceModel, duration, score,
                efficiency, latency, spo2Min, snore,
                apnea, insomnia, stress, medication,
                deep, light, rem, awake,
                hr, steps, caffeine, screen,
                temp, noise
        );
    }

    private boolean isInvalidDouble(double val) {
        return Double.isNaN(val) || Double.isInfinite(val);
    }

    public Map<String, Object> getRangeLimits() {
        String sql = "SELECT " +
                "  MIN(age) AS minAge, MAX(age) AS maxAge, " +
                "  MIN(sleep_score) AS minSleepScore, MAX(sleep_score) AS maxSleepScore, " +
                "  MIN(snore_events) AS minSnoreEvents, MAX(snore_events) AS maxSnoreEvents, " +
                "  MIN(bmi) AS minBmi, MAX(bmi) AS maxBmi " +
                "FROM dwd_sleep_detail";
        try {
            return jdbcTemplate.queryForMap(sql);
        } catch (Exception e) {
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("minAge", 18);
            fallback.put("maxAge", 80);
            fallback.put("minSleepScore", 40);
            fallback.put("maxSleepScore", 100);
            fallback.put("minSnoreEvents", 0);
            fallback.put("maxSnoreEvents", 50);
            fallback.put("minBmi", 15.0);
            fallback.put("maxBmi", 35.0);
            return fallback;
        }
    }

    private Map<String, Long> getGenderRatioFromMySql(SearchRequest req) {
        StringBuilder sql = new StringBuilder("SELECT gender, COUNT(DISTINCT user_id) AS cnt FROM dwd_sleep_detail WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (req.getUserId() != null && !req.getUserId().trim().isEmpty()) {
            sql.append("AND user_id = ? ");
            params.add(req.getUserId().trim());
        }
        if (req.getGender() != null && !req.getGender().trim().isEmpty()) {
            sql.append("AND gender = ? ");
            params.add(req.getGender().trim());
        }
        if (req.getMedicationFlag() != null) {
            sql.append("AND medication_flag = ? ");
            params.add(req.getMedicationFlag());
        }
        if (req.getInsomniaFlag() != null) {
            sql.append("AND insomnia_flag = ? ");
            params.add(req.getInsomniaFlag());
        }
        if (req.getRegion() != null && !req.getRegion().trim().isEmpty()) {
            sql.append("AND region = ? ");
            params.add(req.getRegion().trim());
        }
        if (req.getDeviceModel() != null && !req.getDeviceModel().trim().isEmpty()) {
            sql.append("AND device_model = ? ");
            params.add(req.getDeviceModel().trim());
        }
        if (req.getAgeMin() != null) {
            sql.append("AND age >= ? ");
            params.add(req.getAgeMin());
        }
        if (req.getAgeMax() != null) {
            sql.append("AND age <= ? ");
            params.add(req.getAgeMax());
        }
        if (req.getSleepScoreMin() != null) {
            sql.append("AND sleep_score >= ? ");
            params.add(req.getSleepScoreMin());
        }
        if (req.getSleepScoreMax() != null) {
            sql.append("AND sleep_score <= ? ");
            params.add(req.getSleepScoreMax());
        }
        if (req.getSnoreEventsMin() != null) {
            sql.append("AND snore_events >= ? ");
            params.add(req.getSnoreEventsMin());
        }
        if (req.getSnoreEventsMax() != null) {
            sql.append("AND snore_events <= ? ");
            params.add(req.getSnoreEventsMax());
        }
        if (req.getBmiMin() != null) {
            sql.append("AND bmi >= ? ");
            params.add(req.getBmiMin());
        }
        if (req.getBmiMax() != null) {
            sql.append("AND bmi <= ? ");
            params.add(req.getBmiMax());
        }
        sql.append("GROUP BY gender");

        Map<String, Long> genderMap = new HashMap<>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            for (Map<String, Object> row : list) {
                String gender = (String) row.get("gender");
                Long cnt = ((Number) row.get("cnt")).longValue();
                if (gender != null) {
                    genderMap.put(gender.toLowerCase().trim(), cnt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return genderMap;
    }

    private Map<String, Long> getRegionRatioFromMySql(SearchRequest req) {
        StringBuilder sql = new StringBuilder("SELECT region, COUNT(DISTINCT user_id) AS cnt FROM dwd_sleep_detail WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (req.getUserId() != null && !req.getUserId().trim().isEmpty()) {
            sql.append("AND user_id = ? ");
            params.add(req.getUserId().trim());
        }
        if (req.getGender() != null && !req.getGender().trim().isEmpty()) {
            sql.append("AND gender = ? ");
            params.add(req.getGender().trim());
        }
        if (req.getMedicationFlag() != null) {
            sql.append("AND medication_flag = ? ");
            params.add(req.getMedicationFlag());
        }
        if (req.getInsomniaFlag() != null) {
            sql.append("AND insomnia_flag = ? ");
            params.add(req.getInsomniaFlag());
        }
        if (req.getRegion() != null && !req.getRegion().trim().isEmpty()) {
            sql.append("AND region = ? ");
            params.add(req.getRegion().trim());
        }
        if (req.getDeviceModel() != null && !req.getDeviceModel().trim().isEmpty()) {
            sql.append("AND device_model = ? ");
            params.add(req.getDeviceModel().trim());
        }
        if (req.getAgeMin() != null) {
            sql.append("AND age >= ? ");
            params.add(req.getAgeMin());
        }
        if (req.getAgeMax() != null) {
            sql.append("AND age <= ? ");
            params.add(req.getAgeMax());
        }
        if (req.getSleepScoreMin() != null) {
            sql.append("AND sleep_score >= ? ");
            params.add(req.getSleepScoreMin());
        }
        if (req.getSleepScoreMax() != null) {
            sql.append("AND sleep_score <= ? ");
            params.add(req.getSleepScoreMax());
        }
        if (req.getSnoreEventsMin() != null) {
            sql.append("AND snore_events >= ? ");
            params.add(req.getSnoreEventsMin());
        }
        if (req.getSnoreEventsMax() != null) {
            sql.append("AND snore_events <= ? ");
            params.add(req.getSnoreEventsMax());
        }
        if (req.getBmiMin() != null) {
            sql.append("AND bmi >= ? ");
            params.add(req.getBmiMin());
        }
        if (req.getBmiMax() != null) {
            sql.append("AND bmi <= ? ");
            params.add(req.getBmiMax());
        }
        sql.append("GROUP BY region");

        Map<String, Long> regionMap = new HashMap<>();
        try {
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql.toString(), params.toArray());
            for (Map<String, Object> row : list) {
                String region = (String) row.get("region");
                Long cnt = ((Number) row.get("cnt")).longValue();
                if (region != null) {
                    regionMap.put(region.trim(), cnt);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return regionMap;
    }
}

