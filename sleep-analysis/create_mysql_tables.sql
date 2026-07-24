-- create_mysql_tables.sql
-- 作用：创建 Spring Boot 服务层使用的 MySQL 表结构
-- 使用方式：
--   Get-Content -Path "create_mysql_tables.sql" -Raw -Encoding utf8 | docker exec -i mysql mysql -uroot -proot123 sleep_dashboard

CREATE DATABASE IF NOT EXISTS sleep_dashboard DEFAULT CHARACTER SET utf8mb4;
USE sleep_dashboard;

-- ========== 认证与权限：用户表 ==========
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64) NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    nickname    VARCHAR(64),
    avatar      VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
INSERT INTO sys_user (username, password, nickname) VALUES ('root', '5e5525e2955723b12b338fa00c136028', '管理员');


-- ========== 屏1：睡眠健康总览 ==========
DROP TABLE IF EXISTS dws_yearly_trend;
CREATE TABLE dws_yearly_trend (
    year_recorded       INT PRIMARY KEY,
    avg_sleep_score      DOUBLE,
    avg_duration_minutes DOUBLE,
    avg_efficiency_pct   DOUBLE,
    insomnia_ratio_pct   DOUBLE,
    high_apnea_risk_cnt  BIGINT,
    record_cnt           BIGINT
);

DROP TABLE IF EXISTS dws_monthly_trend;
CREATE TABLE dws_monthly_trend (
    year_recorded     INT,
    month_recorded    INT,
    avg_sleep_score    DOUBLE,
    avg_efficiency_pct DOUBLE,
    PRIMARY KEY (year_recorded, month_recorded)
);

DROP TABLE IF EXISTS dws_region_summary;
CREATE TABLE dws_region_summary (
    region               VARCHAR(64) PRIMARY KEY,
    avg_duration_minutes DOUBLE,
    avg_sleep_score      DOUBLE,
    user_cnt             BIGINT
);

DROP TABLE IF EXISTS dws_device_share;
CREATE TABLE dws_device_share (
    device_model VARCHAR(64) PRIMARY KEY,
    cnt          BIGINT,
    pct          DOUBLE
);


-- ========== 屏2：个人睡眠画像 ==========
-- 增加 DWD 明细数据表，作为个人画像的核心数据来源
DROP TABLE IF EXISTS dwd_sleep_detail;
CREATE TABLE dwd_sleep_detail (
    user_id                        VARCHAR(32),
    date_recorded                  VARCHAR(32),
    year_recorded                  INT,
    month_recorded                 INT,
    age                            INT,
    age_bucket                     VARCHAR(16),
    gender                         VARCHAR(16),
    weight_kg                      DOUBLE,
    height_cm                      DOUBLE,
    bmi                            DOUBLE,
    region                         VARCHAR(64),
    device_model                   VARCHAR(64),
    sleep_duration_minutes         DOUBLE,
    sleep_score                    INT,
    sleep_efficiency_pct           DOUBLE,
    sleep_latency_minutes          DOUBLE,
    wakeup_count                   INT,
    awake_time_minutes             DOUBLE,
    deep_sleep_minutes             DOUBLE,
    light_sleep_minutes            DOUBLE,
    rem_sleep_minutes              DOUBLE,
    deep_sleep_pct                 DOUBLE,
    light_sleep_pct               DOUBLE,
    rem_sleep_pct                  DOUBLE,
    awake_pct                      DOUBLE,
    heart_rate_mean_bpm            DOUBLE,
    hrv_rmssd_ms                   DOUBLE,
    spo2_mean_pct                  DOUBLE,
    spo2_min_pct                   DOUBLE,
    respiration_rate_mean_bpm      DOUBLE,
    step_count_day                 INT,
    snore_events                   INT,
    apnea_risk_score               INT,
    insomnia_flag                  INT,
    stress_score                   INT,
    caffeine_mg                    DOUBLE,
    alcohol_units                  DOUBLE,
    screen_time_before_bed_min     DOUBLE,
    activity_before_bed_min        DOUBLE,
    room_temperature_c             DOUBLE,
    room_humidity_pct              DOUBLE,
    ambient_noise_db               DOUBLE,
    nap_duration_minutes           DOUBLE,
    medication_flag                INT,
    bedtime_consistency_std_min    DOUBLE,
    PRIMARY KEY (user_id, date_recorded),
    INDEX idx_user_id (user_id)
);

DROP TABLE IF EXISTS dws_age_stage_breakdown;
CREATE TABLE dws_age_stage_breakdown (
    age_bucket    VARCHAR(16) PRIMARY KEY,
    avg_deep_pct  DOUBLE,
    avg_light_pct DOUBLE,
    avg_rem_pct   DOUBLE,
    avg_awake_pct DOUBLE
);

DROP TABLE IF EXISTS dws_snore_apnea_bubble;
CREATE TABLE dws_snore_apnea_bubble (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        VARCHAR(32),
    date_recorded  DATE,
    snore_events   INT,
    spo2_min_pct   DOUBLE,
    apnea_risk_score INT,
    INDEX idx_spo2 (spo2_min_pct),
    INDEX idx_apnea (apnea_risk_score)
);


-- ========== 屏3：生活因子关联 ==========
DROP TABLE IF EXISTS ads_correlation_matrix;
CREATE TABLE ads_correlation_matrix (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    feature_x   VARCHAR(64),
    feature_y   VARCHAR(64),
    corr_value  DOUBLE,
    UNIQUE KEY uk_feature_pair (feature_x, feature_y)
);

DROP TABLE IF EXISTS ads_temperature_efficiency;
CREATE TABLE ads_temperature_efficiency (
    temp_bucket        VARCHAR(16) PRIMARY KEY,
    avg_efficiency_pct DOUBLE,
    record_cnt         BIGINT
);

DROP TABLE IF EXISTS ads_activity_latency;
CREATE TABLE ads_activity_latency (
    activity_group           VARCHAR(32) PRIMARY KEY,
    avg_sleep_latency_minutes DOUBLE,
    record_cnt                BIGINT
);


-- ========== 屏4：智能诊断预测 ==========
DROP TABLE IF EXISTS ads_feature_importance;
CREATE TABLE ads_feature_importance (
    feature_name    VARCHAR(64) PRIMARY KEY,
    importance      DOUBLE,
    importance_pct  DOUBLE
);

DROP TABLE IF EXISTS ads_model_metrics;
CREATE TABLE ads_model_metrics (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    model_name     VARCHAR(64),
    rmse           DOUBLE,
    r2             DOUBLE,
    feature_count  INT,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

DROP TABLE IF EXISTS ads_user_cluster;
CREATE TABLE ads_user_cluster (
    user_id    VARCHAR(32) PRIMARY KEY,
    cluster_id INT,
    pca_1      DOUBLE,
    pca_2      DOUBLE,
    pca_3      DOUBLE
);

DROP TABLE IF EXISTS ads_cluster_profile;
CREATE TABLE ads_cluster_profile (
    cluster_id                       INT PRIMARY KEY,
    user_cnt                         BIGINT,
    avg_sleep_score                  DOUBLE,
    avg_sleep_efficiency_pct         DOUBLE,
    avg_heart_rate_bpm               DOUBLE,
    avg_hrv_ms                       DOUBLE,
    avg_spo2_min_pct                 DOUBLE,
    avg_snore_events                 DOUBLE,
    avg_apnea_risk_score             DOUBLE,
    avg_stress_score                 DOUBLE,
    insomnia_ratio                   DOUBLE,
    avg_caffeine_mg                  DOUBLE,
    avg_alcohol_units                DOUBLE,
    avg_screen_time_before_bed_min   DOUBLE,
    cluster_label                    VARCHAR(32) DEFAULT NULL
);


-- ========== 屏5：数据极速检索 ==========
-- 屏5核心数据直接全量同步至 Elasticsearch (sleep_records 索引)
-- MySQL 在本模块不需要额外的专有存储表，直接通过 sys_user 与 dwd_sleep_detail 进行基础的画像校验与鉴权管理
