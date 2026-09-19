@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0"

echo ===================================================
echo  DIGITAL CERTIFICATE SYSTEM - STARTUP
echo ===================================================
echo.
echo Checking for Docker...
docker info >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not running. Please start Docker Desktop and try again.
    pause
    exit /b 1
)

echo Building and starting containers...
docker compose up -d --build

echo.
echo Waiting for application to become healthy...
:waitloop
timeout /t 5 /nobreak >nul
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get; if ($response.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo Still waiting...
    goto waitloop
)

echo.
echo ===================================================
echo [SUCCESS] Application is now RUNNING!
echo.
echo Application URL : http://localhost:8080
echo.
echo You can log in using:
echo Admin Email     : admin@example.com
echo Admin Password  : admin
echo ===================================================
pause
