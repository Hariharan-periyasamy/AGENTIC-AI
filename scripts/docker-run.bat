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
echo [3/3] Waiting for Health...
:waitloop
timeout /t 5 /nobreak >nul
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get; if ($response.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo Still waiting...
    goto waitloop
)

echo.
echo [SUCCESS] Application successfully deployed via Docker!
echo URL: http://localhost:8080
exit /b 0
