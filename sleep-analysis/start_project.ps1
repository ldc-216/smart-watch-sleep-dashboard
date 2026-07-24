# start_project.ps1
# Sleep Health Big Data Platform - One-Click Launcher
# Environment: Windows PowerShell

$ErrorActionPreference = "Continue"
Clear-Host

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "     Sleep Health Big Data Platform - One-Click Launcher   " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan

# 1. Check Docker status
Write-Host "[1/6] Checking Docker service status..." -ForegroundColor Yellow
$dockerInfo = docker info 2>$null
if ($LASTEXITCODE -ne 0 -or $null -eq $dockerInfo) {
    Write-Host "[ERROR] Docker daemon not detected! Please start Docker Desktop." -ForegroundColor Red
    Write-Host "Press any key to exit..."
    $null = [System.Console]::ReadKey($true)
    exit 1
}
Write-Host "[OK] Docker is running normally." -ForegroundColor Green

# 2. Start Docker Compose Cluster
Write-Host "`n[2/6] Starting Big Data Cluster (Docker Compose)..." -ForegroundColor Yellow
if (Test-Path "docker") {
    Push-Location "docker"
    docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Failed to start Docker Compose! Please check ports and docker-compose.yml." -ForegroundColor Red
        Pop-Location
        exit 1
    }
    Pop-Location
} else {
    Write-Host "[ERROR] Folder 'docker' not found! Make sure you are in the project root directory." -ForegroundColor Red
    exit 1
}

# 3. Wait for MySQL to be ready
Write-Host "`n[3/6] Waiting for MySQL service initialization (Max 2 mins)..." -ForegroundColor Yellow
$mysqlReady = $false
for ($i = 1; $i -le 24; $i++) {
    $pingResult = docker exec -e MYSQL_PWD=root123 mysql mysqladmin ping -uroot 2>&1
    if ($pingResult -like "*alive*") {
        $mysqlReady = $true
        break
    }
    Write-Host "[INFO] MySQL starting, second $($i*5)..." -ForegroundColor DarkGray
    Start-Sleep -Seconds 5
}

if (-not $mysqlReady) {
    Write-Host "[ERROR] MySQL startup timed out! Check logs with: docker logs mysql" -ForegroundColor Red
    exit 1
}
Write-Host "[OK] MySQL database is ready!" -ForegroundColor Green

# 4. Initialize MySQL Database Tables
Write-Host "`n[4/6] Initializing MySQL database tables..." -ForegroundColor Yellow
if (Test-Path "create_mysql_tables.sql") {
    Get-Content -Path "create_mysql_tables.sql" -Raw -Encoding utf8 | docker exec -i -e MYSQL_PWD=root123 mysql mysql -uroot sleep_dashboard 2>&1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Database initialization failed!" -ForegroundColor Red
        exit 1
    }
    Write-Host "[OK] Database tables and initial admin user imported successfully!" -ForegroundColor Green
} else {
    Write-Host "[WARN] create_mysql_tables.sql script not found, skipping initialization." -ForegroundColor Yellow
}

# 5. Execute Spark Pipeline Job
Write-Host "`n[5/6] Starting Spark offline processing and ML Pipeline (Takes 1-2 mins)..." -ForegroundColor Yellow
if (Test-Path "run_pipeline.ps1") {
    # Auto-ensure numpy is installed inside Spark master and worker containers (ignoring pip root user warnings)
    Write-Host "[INFO] Ensuring Python dependencies (numpy) in Spark Master..." -ForegroundColor DarkGray
    docker exec -u root spark-master pip install numpy -q --root-user-action=ignore 2>&1
    Write-Host "[INFO] Ensuring Python dependencies (numpy) in Spark Worker 1..." -ForegroundColor DarkGray
    docker exec -u root spark-worker-1 pip install numpy -q --root-user-action=ignore 2>&1
    Write-Host "[INFO] Ensuring Python dependencies (numpy) in Spark Worker 2..." -ForegroundColor DarkGray
    docker exec -u root spark-worker-2 pip install numpy -q --root-user-action=ignore 2>&1

    & .\run_pipeline.ps1
    if ($LASTEXITCODE -ne 0) {
        Write-Host "[ERROR] Spark Pipeline execution failed!" -ForegroundColor Red
        exit 1
    }
} else {
    Write-Host "[ERROR] run_pipeline.ps1 script not found!" -ForegroundColor Red
    exit 1
}

# Verify PMML file
$pmmlPath = "sleep-dashboard-backend\src\main\resources\models\sleep_score_rf.pmml"
if (Test-Path $pmmlPath) {
    Write-Host "[OK] PMML model verified successfully in backend resources." -ForegroundColor Green
} else {
    Write-Host "[WARN] PMML model file not found in backend resources! Backend might fail to start." -ForegroundColor Yellow
}

# 6. Launch Backend and Frontend in separate windows
Write-Host "`n[6/6] Launching Frontend and Backend consoles..." -ForegroundColor Yellow

# Backend
if (Test-Path "sleep-dashboard-backend") {
    Write-Host "[INFO] Launching Spring Boot Backend console (Maven)..." -ForegroundColor DarkGray
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "
        Write-Host '==================================================' -ForegroundColor Cyan
        Write-Host '          Spring Boot Backend Server             ' -ForegroundColor Cyan
        Write-Host '==================================================' -ForegroundColor Cyan
        cd sleep-dashboard-backend
        mvn spring-boot:run
    " -WindowStyle Normal
} else {
    Write-Host "[ERROR] Folder 'sleep-dashboard-backend' not found, cannot launch backend!" -ForegroundColor Red
}

# Frontend
if (Test-Path "vue-frontend") {
    Write-Host "[INFO] Launching Vue 3 Frontend console (Vite)..." -ForegroundColor DarkGray
    Start-Process powershell -ArgumentList "-NoExit", "-Command", "
        Write-Host '==================================================' -ForegroundColor Cyan
        Write-Host '            Vue3 Frontend Dashboard               ' -ForegroundColor Cyan
        Write-Host '==================================================' -ForegroundColor Cyan
        cd vue-frontend
        Write-Host '[INFO] Checking node_modules...' -ForegroundColor Yellow
        if (-not (Test-Path 'node_modules')) {
            npm install
        }
        npm run dev
    " -WindowStyle Normal
} else {
    Write-Host "[ERROR] Folder 'vue-frontend' not found, cannot launch frontend!" -ForegroundColor Red
}

Write-Host "`n============================================================" -ForegroundColor Green
Write-Host "        Cluster and Services Launched Successfully!        " -ForegroundColor Green
Write-Host "============================================================" -ForegroundColor Green
Write-Host "[1] Frontend Dashboard : http://localhost:5173" -ForegroundColor Cyan
Write-Host "[2] Backend API Swagger: http://localhost:8888/swagger-ui.html" -ForegroundColor Cyan
Write-Host "[3] Spark Web UI       : http://localhost:8080" -ForegroundColor Cyan
Write-Host "[4] HDFS Web Console   : http://localhost:9870" -ForegroundColor Cyan
Write-Host "[5] Elasticsearch Info : http://localhost:9200" -ForegroundColor Cyan
Write-Host "[6] Kibana Console     : http://localhost:5601" -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Green
Write-Host "Note: Please check the two new PowerShell windows for logs." -ForegroundColor Yellow
Write-Host "============================================================" -ForegroundColor Green
