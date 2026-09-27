@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."

echo =====================================
echo  DOCKER DEPLOYMENT
echo =====================================

echo [1/3] Building Docker Image...
docker compose build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker build failed.
    exit /b %ERRORLEVEL%
)

echo.
echo [2/3] Starting Containers...
docker compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to start containers.
    exit /b %ERRORLEVEL%
)

echo.
echo [3/3] Waiting for Health (max 30 attempts)...
set /a attempt=0
set MAX_ATTEMPTS=30

:waitloop
set /a attempt+=1
timeout /t 5 /nobreak >nul
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get -TimeoutSec 5; if ($response.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [SUCCESS] Application successfully deployed via Docker!
    echo URL: http://localhost:8080
    exit /b 0
)

echo Still waiting... (Attempt !attempt!/%MAX_ATTEMPTS%)
if !attempt! LSS %MAX_ATTEMPTS% goto waitloop

echo.
echo [ERROR] Health check timed out after %MAX_ATTEMPTS% attempts.
echo Showing container logs:
docker logs dcs_app --tail 50
exit /b 1
