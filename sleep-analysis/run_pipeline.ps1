# run_pipeline.ps1
# Sleep Health Big Data Platform - Spark Consolidated Pipeline Runner

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "         Sleep Health Big Data Platform - Pipeline Runner" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Submit Spark Job
Write-Host "[INFO] Submitting consolidated Spark pipeline job..." -ForegroundColor Yellow

# Pass -u root to avoid UnixLoginModule NPE (NullPointerException: invalid null input: name)
docker exec -u root -i spark-master /opt/bitnami/spark/bin/spark-submit --master spark://spark-master:7077 --jars /opt/spark-apps/mysql-connector-j-8.0.33.jar --conf spark.sql.warehouse.dir=hdfs://namenode:9000/user/hive/warehouse /opt/spark-apps/run_pipeline.py

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n[OK] Spark consolidated pipeline job completed successfully!" -ForegroundColor Green
    
    # 2. Sync PMML Model to Spring Boot Backend
    $pmml_src = "spark-apps\sleep_score_rf.pmml"
    $pmml_dst = "sleep-dashboard-backend\src\main\resources\models\sleep_score_rf.pmml"
    
    Write-Host "[INFO] Syncing PMML prediction model to Spring Boot backend..." -ForegroundColor Yellow
    if (Test-Path $pmml_src) {
        $dst_dir = [System.IO.Path]::GetDirectoryName($pmml_dst)
        if (-not (Test-Path $dst_dir)) {
            New-Item -ItemType Directory -Force -Path $dst_dir | Out-Null
        }
        Copy-Item -Path $pmml_src -Destination $pmml_dst -Force
        Write-Host "[OK] Model successfully synced to: $pmml_dst" -ForegroundColor Green
    } else {
        Write-Host "[WARN] PMML model not found at $pmml_src, skipping sync." -ForegroundColor Yellow
    }
} else {
    Write-Host "`n[ERROR] Spark pipeline job failed. Please check the logs above." -ForegroundColor Red
}

Write-Host "`n============================================================" -ForegroundColor Cyan
Write-Host "                         Task Finished" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
