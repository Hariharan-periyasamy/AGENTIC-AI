@echo off
cd /d "%~dp0.."

echo =====================================
echo  STOPPING APPLICATION
echo =====================================

docker compose down
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Failed to gracefully stop Docker Compose containers.
    exit /b %ERRORLEVEL%
)

echo [OK] Application containers stopped safely.
exit /b 0
