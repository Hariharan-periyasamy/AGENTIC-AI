@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."

:: Detect Maven (it may not be on PATH — check known location first)
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=C:\apache-maven-3.9.16\bin\mvn.cmd"
    )
)

:: Initialize result variables
set RES_BUILD=FAIL
set RES_TESTS=FAIL
set RES_DOCKER=FAIL
set RES_DB=FAIL
set RES_HEALTH=FAIL
set RES_APP=FAIL

echo =====================================
echo  DIGITAL CERTIFICATE SYSTEM
echo  DEVOPS AUTOMATION
echo =====================================
echo.

:: ---------------------------------------------------------
echo [1/7] Checking prerequisites...
:: ---------------------------------------------------------
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Java not found. & goto end_fail )
call %MVN_CMD% -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Maven not found at '%MVN_CMD%'. & goto end_fail )
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [ERROR] Docker not found. & goto end_fail )

:: ---------------------------------------------------------
echo.
echo [2/7] Building application...
:: ---------------------------------------------------------
call %MVN_CMD% clean compile
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Build failed.
    goto end_fail
)
set RES_BUILD=PASS

:: ---------------------------------------------------------
echo.
echo [3/7] Running tests...
:: ---------------------------------------------------------
call %MVN_CMD% test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Tests failed.
    goto end_fail
)
set RES_TESTS=PASS

:: ---------------------------------------------------------
echo.
echo [4/7] Packaging Application...
:: ---------------------------------------------------------
call %MVN_CMD% package -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Packaging failed.
    goto end_fail
)

:: ---------------------------------------------------------
echo.
echo [5/7] Building Docker image ^& Starting containers...
:: ---------------------------------------------------------
docker compose build
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker build failed.
    goto end_fail
)

docker compose up -d
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker compose up failed.
    goto end_fail
)
set RES_DOCKER=PASS

:: Verify DB running
docker ps | findstr dcs_mysql >nul
if %ERRORLEVEL% EQU 0 set RES_DB=PASS

:: ---------------------------------------------------------
echo.
echo [6/7] Health checking...
:: ---------------------------------------------------------
echo Waiting up to 60 seconds for application to boot...
set /a attempt=0
:health_loop
timeout /t 5 /nobreak >nul
set /a attempt+=1
powershell -Command "try { $response = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -Method Get; if ($response.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% EQU 0 (
    set RES_HEALTH=PASS
    goto end_health_loop
)
if !attempt! LSS 12 (
    echo Still waiting... ^(Attempt !attempt!/12^)
    goto health_loop
)
echo [ERROR] Health check timed out.
goto end_fail
:end_health_loop
echo [OK] Application is healthy.

:: ---------------------------------------------------------
echo.
echo [7/7] Final verification...
:: ---------------------------------------------------------
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method Get -UseBasicParsing; if ($response.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Smoke test failed.
    goto end_fail
)
set RES_APP=PASS

:: ---------------------------------------------------------
:: Success Block
:: ---------------------------------------------------------
echo.
echo =====================================
echo  FINAL RESULT
echo =====================================
echo Build       : !RES_BUILD!
echo Tests       : !RES_TESTS!
echo Docker      : !RES_DOCKER!
echo Database    : !RES_DB!
echo Health      : !RES_HEALTH!
echo Application : !RES_APP!
echo.
echo OVERALL RESULT: PASS
echo =====================================
exit /b 0

:: ---------------------------------------------------------
:: Failure Block
:: ---------------------------------------------------------
:end_fail
echo.
echo =====================================
echo  FINAL RESULT
echo =====================================
echo Build       : !RES_BUILD!
echo Tests       : !RES_TESTS!
echo Docker      : !RES_DOCKER!
echo Database    : !RES_DB!
echo Health      : !RES_HEALTH!
echo Application : !RES_APP!
echo.
echo OVERALL RESULT: FAIL
echo =====================================
exit /b 1
