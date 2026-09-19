@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."

echo =====================================
echo  STARTING APPLICATION
echo =====================================

:: Check Docker daemon is running
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker daemon is not running. Please start Docker Desktop.
    exit /b 1
)

echo Starting MySQL and Application via Docker Compose...
docker compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] docker compose up failed.
    exit /b 1
)

echo Waiting for application to become healthy (max 30 attempts x 5s = 150s)...
set /a attempt=0
set MAX_ATTEMPTS=30

:waitloop
set /a attempt+=1
timeout /t 5 /nobreak >nul
powershell -Command "try { $r = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 4; if ($r.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% EQU 0 (
    echo.
    echo [OK] Application is running and healthy!
    echo [URL] http://localhost:8080
    exit /b 0
)

echo Still waiting... (Attempt !attempt!/%MAX_ATTEMPTS%)
if !attempt! LSS %MAX_ATTEMPTS% goto waitloop

echo.
echo [ERROR] APPLICATION FAILED TO START after %MAX_ATTEMPTS% attempts.
echo Showing container logs:
docker logs dcs_app --tail 50
exit /b 1
