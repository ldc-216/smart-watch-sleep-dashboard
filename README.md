# 智能手表睡眠监测与分析大屏系统

基于 Spark 的智能手表睡眠健康数据分析与可视化大屏系统。采集智能手表睡眠/生理数据，经 Hadoop/Spark 数仓 ETL 清洗，使用随机森林模型进行睡眠评分与预测，最终通过 Vue 大屏 + Java(Spring Boot) 后端 + Elasticsearch 提供多维度分析与预警。

> 信阳师范大学 计算机学院 · 数据科学与大数据技术专业一班 · 第 4 组 专业实训项目

## 功能特性

- **睡眠结构与生理特征分析**：睡眠分期、心率/血氧/体动等生理指标可视化
- **生活与环境因素关联分析**：作息、运动、环境噪声等因素与睡眠质量关联挖掘
- **智能预测与诊断预警**：基于随机森林（PMML 模型）的睡眠评分与异常预警
- **一站式大屏**：多主题可视化大屏，支持实时查询与推荐

## 技术栈

| 层 | 技术 |
| --- | --- |
| 数据采集 / 存储 | MySQL、Hadoop HDFS |
| 数据处理 | Spark（Python / Scala）、PMML（JPMML）、随机森林 |
| 后端 | Java / Spring Boot、Elasticsearch |
| 前端 | Vue 3 + Vite、ECharts 大屏 |
| 部署 | Docker / docker-compose |

## 目录结构

```
.
├── sleep-analysis/          # 系统源码（详见其内 README.md）
│   ├── docker/              # docker-compose 编排与配置
│   ├── sleep-dashboard-backend/   # Spring Boot 后端
│   ├── vue-frontend/        # Vue 大屏前端
│   ├── spark-apps/          # Spark 清洗 / 评分 / PMML 推理
│   ├── create_mysql_tables.sql    # 数据库建表
│   ├── run_pipeline.ps1 / .py     # 数据处理流水线
│   ├── 一键启动.bat / start_project.ps1  # 快速启动
│   ├── API_SPEC.md          # 接口规范
│   └── 睡眠健康大数据平台答辩PPT_v2.pptx
└── docs/                    # 项目文档
    ├── 信师大2025-2026学年第二学期专业实训-专业实训报告_v2.docx
    ├── 信师大2025-2026学年第二学期专业实训-需求分析报告第四组.docx
    ├── 睡眠健康平台_需求分析报告.docx
    ├── 团队角色与分工.md
    ├── 答辩话术_*.md
    └── ...（格式规范、合规检查、品牌素材等）
```

## 快速开始

详见 [`sleep-analysis/README.md`](sleep-analysis/README.md)。概要：

```bash
# 1. 启动基础设施（MySQL / Hadoop / Spark / Elasticsearch）
cd sleep-analysis/docker && docker-compose up -d

# 2. 初始化数据库
mysql -u root -p < ../create_mysql_tables.sql

# 3. 运行 Spark 数据处理流水线
cd ../spark-apps && python run_pipeline.py

# 4. 启动后端
cd ../sleep-dashboard-backend && mvn spring-boot:run

# 5. 启动前端大屏
cd ../vue-frontend && npm install && npm run dev
```

也可直接使用 `sleep-analysis/一键启动.bat` / `start_project.ps1` 一键拉起。

## 团队（信阳师范大学 计算机学院 · 数据科学与大数据技术专业一班 · 第 4 组）

| 成员 | 角色 |
| --- | --- |
| 赵国旭 | 系统源码主要作者（前端 / 后端 / Spark 应用） |
| 李定晨 | 项目上传与 GitHub 仓库维护 |
| 杜心浩、王宛强、白天航 | 项目组成员 |

本仓库由 **李定晨** 整理并上传至 GitHub。

## 许可证

本项目为高校专业实训作品，仅供学习交流使用。
