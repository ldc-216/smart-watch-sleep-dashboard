# 睡眠健康大数据平台 (Sleep Health Big Data Platform)

[![Hadoop](https://img.shields.io/badge/Hadoop-3.2.1-red.svg)](https://hadoop.apache.org/)
[![Spark](https://img.shields.io/badge/Spark-3.5.6-orange.svg)](https://spark.apache.org/)
[![Hive](https://img.shields.io/badge/Hive-2.3.2-blue.svg)](https://hive.apache.org/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-2.7.18-green.svg)](https://spring.io/projects/spring-boot)
[![Vue](https://img.shields.io/badge/Vue-3.0-brightgreen.svg)](https://vuejs.org/)
[![Docker](https://img.shields.io/badge/Docker-Supported-success.svg)](#)

这是一个面向毕业设计与大数据实践的**高性能分布式睡眠健康大数据平台**。平台涵盖了：**数据分布式存储 (HDFS 集群) ➔ 离线数仓建模 (Hive) ➔ 大数据分布式计算 (Spark 真实集群) ➔ 机器学习模型训练与同步 (Spark MLlib/PMML) ➔ 高性能持久化 (MySQL & Elasticsearch) ➔ 业务后端 (Spring Boot) ➔ 大数据可视化大屏 (Vue3 & Echarts & Echarts-GL) ➔ AI 智能诊断顾问 (DeepSeek 大模型)** 的全流程闭环架构。

本项目经过**深度性能调优与集群化改造**，采用 **3 DataNode + 2 Spark Worker** 的真实分布式集群架构，通过多维度调优将流水线运行时间缩短了数倍。

---

## 🧭 系统架构与数据流向

```mermaid
graph TD
    %% 数据源
    subgraph DataSource [数据源]
        CSV[smartwatch_sleep_dataset.csv<br/>智能手表睡眠原始数据集]
    end

    %% 容器基础架构
    subgraph BigDataCluster [分布式大数据集群 (Docker)]
        HDFS_NN[HDFS NameNode]
        HDFS_DN[HDFS DataNodes<br/>datanode1 / datanode2 / datanode3<br/>三副本冗余存储]
        Hive[Hive Data Warehouse<br/>数仓建模 ODS/DWD/DWS/ADS]
        Spark_Master[Spark Master]
        Spark_Workers[Spark Workers<br/>spark-worker-1 / spark-worker-2<br/>分布式并行计算]
        ES[Elasticsearch 7.17.9<br/>1分片 0副本 强类型索引]
    end

    %% 服务与持久化
    subgraph AppService [应用与服务层]
        MySQL[(MySQL 8.0)<br/>HikariCP 连接池调优]
        SpringBoot[Spring Boot 2.7.18 后端<br/>MyBatis-Plus 日志优化]
        PMML[PMML Evaluator<br/>线程安全元数据缓存 & 实时评估]
        DeepSeek[DeepSeek API<br/>AI 智能诊断]
    end

    %% 展示层
    subgraph FrontendApp [前端可视化大屏 (Vue3)]
        Vue[Vue3 + Vite5]
        Echarts[Echarts 5.5.0<br/>大屏可视化与检索]
        EchartsGL[Echarts-GL<br/>PCA 3D 散点聚类]
    end

    %% 数据流指向
    CSV -->|1. 上传数据| HDFS_NN
    HDFS_NN -->|2. 分布式副本同步| HDFS_DN
    HDFS_DN -->|3. 创建外表与加载| Hive
    Spark_Workers -->|4. 读取 Hive & 并行清洗与特征工程| Spark_Workers
    Spark_Workers -->|5. 训练随机森林模型| PMML_File[sleep_score_rf.pmml]
    Spark_Master -->|6. Driver 端批量快速写入| ES
    Spark_Workers -->|7. 统计指标与明细批量重写写入| MySQL
    PMML_File -->|8. 模型同步| PMML
    PMML -->|9. 毫秒级在线推理| SpringBoot
    SpringBoot -->|10. 极速条件检索| ES
    SpringBoot -->|11. 连接与指标查询| MySQL
    SpringBoot -->|12. 大模型诊断| DeepSeek
    Vue -->|13. RESTful API 请求| SpringBoot
    Vue -->|14. 页面渲染| Echarts & EchartsGL
```

---

## ⚡ 核心优化与集群设计说明

本项目针对本地容器环境进行了多项企业级性能调优：
1.  **真实分布式集群**：
    *   **HDFS 扩容**：部署 1 个 NameNode + 3 个独立 DataNode 节点，配置 `dfs.replication=3` 三副本冗余。
    *   **Spark 扩容**：部署 1 个 Master + 2 个 Worker 工作节点，实现真正的分布式 Partition 并行计算，流水线运行耗时从 **3分钟+ 缩短至 71秒**。
2.  **Spark 调度开销优化**：
    *   将 `spark.sql.shuffle.partitions` 从默认 the 200 限制为 **4**，大幅减少了小数据集在 Docker 中的 Task 线程调度延迟。
3.  **消灭 Python UDF 损耗**：
    *   将年龄分桶、温度分桶等 Python 自定义函数（UDF）全部改写为原生 Spark 内置函数 `F.when().otherwise()`，实现全 JVM 内部运行，避免跨进程通信开销。
4.  **写入通道优化**：
    *   **ES 批量化**：在 Driver 端直接执行批量（Bulk）HTTP 写入，副本数设为 0，显式声明 Mapping 强类型，省去 RDD 转换开销。
    *   **MySQL 批处理**：开启 `rewriteBatchedStatements=true` 批量重写并配置 `batchsize=5000`。
5.  **后端与模型评估压榨**：
    *   **MyBatis 日志优化**：在生产环境中采用 SLF4J 异步日志，关闭控制台 Stdout 同步 SQL 输出，解决高并发下的 CPU 锁瓶颈。
    *   **HikariCP 调优**：对 MySQL 连接池的最大、最小空闲连接和超时时间进行科学调优。
    *   **PMML 元数据预热**：在服务启动时提前解析并缓存 PMML 模型的输入/目标字段定义，推理时无需每次创建 Map 容器，实现毫秒级响应。

---

## 📂 项目目录结构

```plaintext
睡眠分析/
├── docker/                      # Docker 部署目录
│   ├── docker-compose.yml       # 3节点 HDFS、2节点 Spark、Hive、MySQL、ES 容器编排文件
│   ├── hadoop.env               # Hadoop 环境变量配置 (含三副本设置)
│   └── spark-conf/              # Spark 挂载 of Core/Hive 配置文件
├── spark-apps/                  # Spark 分布式数据处理与机器学习程序
│   ├── run_pipeline.py          # 核心代码：清洗、建表、指标计算、聚类、随机森林训练、数据同步
│   ├── mysql-connector-j-*.jar  # JDBC 连接驱动包
│   └── sleep_score_rf.pmml      # 训练后生成的 PMML 格式模型文件
├── sleep-dashboard-backend/     # Spring Boot 业务后端服务
│   ├── src/main/java/           # Java 源代码
│   ├── src/main/resources/
│   │   ├── application.yml      # 后端配置文件 (含数据库、ES连接、DeepSeek Key、连接池配置)
│   │   └── models/              # 同步过来的 PMML 模型文件存放处
│   └── pom.xml                  # Maven 依赖配置
├── vue-frontend/                # Vue3 可视化大屏前端
│   ├── src/                     # 前端源代码
│   ├── package.json             # 前端 Node 依赖配置
│   └── vite.config.js           # Vite 配置文件
├── smartwatch_sleep_dataset.csv # 原始睡眠健康数据集 (5M+, 2万余条数据)
├── create_mysql_tables.sql      # MySQL 初始化表结构及管理员用户导入脚本
├── run_pipeline.ps1             # 提交 Spark 任务与模型文件同步的 PowerShell 脚本
└── start_project.ps1            # 【推荐】Windows 下一键自动化部署与启动脚本
```

---

## 🚀 完整项目启动命令与步骤

为了运行本项目，请确保宿主机已安装 **Docker Desktop (开启 WSL2)**、**JDK 11** (或 JDK 8)、**Maven 3.6+** 和 **Node.js 18+**。

### 步骤一：启动基础大数据集群 (Docker)
打开命令行窗口，切换至 `docker` 目录，执行以下命令：
```bash
# 1. 进入 docker 配置目录
cd docker

# 2. 停止并彻底清理可能残留的旧单机卷与容器
docker-compose down -v

# 3. 后台拉起 3-DataNode + 2-Worker 分布式集群
docker-compose up -d
```
> **检查集群是否就绪：**
> *   **HDFS 管理台**：访问 [http://localhost:9870](http://localhost:9870)，在 **Datanodes** 菜单中应显示 **3 个 Alive 节点**。
> *   **Spark 管理台**：访问 [http://localhost:8080](http://localhost:8080)，在 **Workers** 列表中应显示 **2 个 Alive 的 Worker**。

---

### 步骤二：初始化 MySQL 数据库表结构
返回项目根目录，在 PowerShell 中执行以下命令，将建表 SQL 导入 MySQL 容器：
```powershell
# 1. 确保在项目根目录下
cd ..

# 2. 将 create_mysql_tables.sql 导入运行中的 mysql 容器
Get-Content -Path "create_mysql_tables.sql" -Raw -Encoding utf8 | docker exec -i mysql mysql -uroot -proot123 sleep_dashboard
```

---

### 步骤三：运行分布式计算与模型训练流水线
在项目根目录下运行流水线脚本：
```powershell
# 1. 提交作业到 Spark 集群，进行数据分层计算与 PMML 模型训练
.\run_pipeline.ps1
```
> 计算完成后，脚本会自动将生成的 `sleep_score_rf.pmml` 模型文件同步拷贝至后端的资源路径下。

---

### 步骤四：启动 Spring Boot 后端服务
打开一个新的终端窗口，进入后端服务目录并运行：
```bash
# 1. 进入后端目录
cd sleep-dashboard-backend

# 2. 编译并启动后端 Spring Boot 项目
mvn spring-boot:run
```
> 服务将监听 **`8888`** 端口，并在控制台打印 `[PmmlConfig] PMML 模型加载成功` 提示。
> API 接口文档地址：[http://localhost:8888/swagger-ui.html](http://localhost:8888/swagger-ui.html)

---

### 步骤五：启动 Vue3 前端大屏
打开另一个新的终端窗口，进入前端目录并运行：
```bash
# 1. 进入前端目录
cd vue-frontend

# 2. 安装项目所需依赖包 (仅首次运行时需要)
npm install

# 3. 启动前端 Vite 调试服务器
npm run dev
```
> 前端启动完成后，在浏览器访问：**[http://localhost:5173/](http://localhost:5173/)** 即可登录查看 5 个大数据看板大屏。
> 默认管理员账号：`root`，密码：`123456`。
