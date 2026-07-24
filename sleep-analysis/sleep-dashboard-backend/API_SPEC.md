# 接口文档 (API_SPEC)

统一响应格式：
```json
{ "code": 200, "message": "ok", "data": ... }
```
Swagger UI: `http://localhost:8888/swagger-ui.html`

---

## 屏1：总体睡眠健康大屏

### GET /api/screen1/overview
一次性返回 KPI + 年度趋势 + 月度趋势 + 地域分布 + 设备份额。

响应 `data` 结构：
```json
{
  "kpi": {
    "avgSleepScore": 71.55, "avgDurationMinutes": 438.24,
    "insomniaRatioPct": 19.05, "highApneaRiskCnt": 576, "totalRecordCnt": 19962
  },
  "yearlyTrend": [{ "yearRecorded": 2024, "avgSleepScore": 63.1, ... }],
  "monthlyTrend": [{ "yearRecorded": 2024, "monthRecorded": 3, ... }],
  "regionSummary": [{ "region": "Europe", "avgSleepScore": 61.8, "userCnt": 320 }],
  "deviceShare": [{ "deviceModel": "AlphaWatch X1", "cnt": 4200, "pct": 12.5 }]
}
```

---

## 屏2：睡眠结构与生理特征分析

### GET /api/screen2/age-stage-breakdown
睡眠阶段堆叠柱状图（按年龄段：under18/18-30/30-45/45-60/60+）

### GET /api/screen2/physiology?gender={female|male}
生理指标雷达图数据源，`gender` 可选，不传返回全部性别x年龄段分组

### GET /api/screen2/snore-apnea-bubble?page=1&size=500
打鼾-血氧气泡图，**明细级数据，必须分页**，默认每页500条

---

## 屏3：生活与环境因素关联分析

### GET /api/screen3/overview
一次性返回：特征相关性矩阵（长表，前端自己 pivot 成热力图）+ 温度-效率分桶 + 睡前运动对比

```json
{
  "correlationMatrix": [{ "featureX": "caffeine_mg", "featureY": "sleep_score", "corrValue": -0.31 }],
  "temperatureEfficiency": [{ "tempBucket": "18-20", "avgEfficiencyPct": 91.2, "recordCnt": 5200 }],
  "activityLatency": [{ "activityGroup": "高频睡前运动", "avgSleepLatencyMinutes": 12.3 }]
}
```

---

## 屏4：智能预测与诊断预警

### POST /api/screen4/predict
预测模拟器，请求体：
```json
{
  "stepCountDay": 5000, "caffeineMg": 100, "alcoholUnits": 0,
  "screenTimeBeforeBedMin": 60, "stressScore": 40, "activityBeforeBedMin": 20,
  "roomTemperatureC": 21, "ambientNoiseDb": 35, "bedtimeConsistencyStdMin": 15,
  "napDurationMinutes": 0, "age": 35, "bmi": 22.5
}
```
响应：`{ "predictedSleepScore": 71.2, "modelName": "RandomForestRegressor" }`

### GET /api/screen4/feature-importance
特征重要性排名条形图，按 importance 降序

### GET /api/screen4/model-metrics
模型评估指标（RMSE/R2），展示模型可信度用

### GET /api/screen4/cluster
人群聚类三维散点图数据：
```json
{
  "points": [{ "userId": "user_00332", "clusterId": 2, "pca1": 1.2, "pca2": -0.3, "pca3": 0.8 }],
  "profiles": [{ "clusterId": 2, "userCnt": 340, "avgApneaRiskScore": 82.1, "clusterLabel": "呼吸暂停高危型", ... }]
}
```
`clusterLabel` 初始是 NULL，需要看完 `profiles` 里的均值特征后手动执行：
```sql
UPDATE ads_cluster_profile SET cluster_label='呼吸暂停高危型' WHERE cluster_id=2;
```

---

## 屏5：极速检索与高危日志明细

### POST /api/screen5/search
多条件搜索，请求体全部字段可选：
```json
{ "userId": "", "gender": "female", "ageMin": 50, "ageMax": null, "medicationFlag": 1, "page": 1, "size": 20 }
```

### GET /api/screen5/alerts?afterDate=2025-06-01&size=30
高危事件流，`afterDate` 用于前端轮询增量查询，不传查最新一批。
前端建议每 5~10 秒调一次，模拟"实时滚动"（技术栈没有 Kafka，不做真流式，见架构说明）。

---

## 尚未覆盖 / 后续可扩展

- 屏5目前只做了搜索和告警流，如果要做"用户详情钻取"（点开某条记录看历史趋势），
  可以加一个 `GET /api/screen5/user/{userId}/history` 接口，直接查 MySQL 或 Hive 都行
- 所有 GET 接口目前都没加权限校验，真实项目应该加 Spring Security 或简单的 Token 拦截器，
  毕设演示阶段可以先不做
