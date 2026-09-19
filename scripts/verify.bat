@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."

:: Detect Maven (may not be on PATH)
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=C:\apache-maven-3.9.16\bin\mvn.cmd"
    )
)

echo =====================================
echo  SYSTEM VERIFICATION
echo =================================================================================
echo.

set PASS_COUNT=0
set FAIL_COUNT=0

:: Helper macros via labels are not native batch; we use inline checks

:: 1. Verify Java
echo [1/10] Checking Java...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [FAIL] Java not found. & set /a FAIL_COUNT+=1 ) else ( echo [PASS] Java & set /a PASS_COUNT+=1 )

:: 2. Verify Maven
echo [2/10] Checking Maven...
cmd.exe /c "%MVN_CMD% -v" >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Maven not found.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Maven
    set /a PASS_COUNT+=1
)

:: 3. Verify Git
echo [3/10] Checking Git...
git --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [FAIL] Git not found. & set /a FAIL_COUNT+=1 ) else ( echo [PASS] Git & set /a PASS_COUNT+=1 )

:: 4. Verify Docker
echo [4/10] Checking Docker...
docker -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [FAIL] Docker not found. & set /a FAIL_COUNT+=1 ) else ( echo [PASS] Docker & set /a PASS_COUNT+=1 )

:: 5. Verify Docker Compose
echo [5/10] Checking Docker Compose...
docker compose version >nul 2>&1
if %ERRORLEVEL% NEQ 0 ( echo [FAIL] Docker Compose not found. & set /a FAIL_COUNT+=1 ) else ( echo [PASS] Docker Compose & set /a PASS_COUNT+=1 )

echo.
echo Verifying Application State...

:: 6. Verify Database Container
echo [6/10] Checking database container...
docker ps 2>nul | findstr dcs_mysql >nul
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Database container dcs_mysql is not running. Run scripts\run.bat first.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Database container dcs_mysql running.
    set /a PASS_COUNT+=1
)

:: 7. Verify Application Container
echo [7/10] Checking application container...
docker ps 2>nul | findstr dcs_app >nul
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Application container dcs_app is not running.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Application container dcs_app running.
    set /a PASS_COUNT+=1
)

:: 8. Verify Application Health
echo [8/10] Checking application health endpoint...
powershell -Command "try { $r = Invoke-RestMethod -Uri 'http://localhost:8080/actuator/health' -TimeoutSec 5; if ($r.status -eq 'UP') { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Health endpoint http://localhost:8080/actuator/health not returning UP.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Health endpoint is UP.
    set /a PASS_COUNT+=1
)

:: 9. Smoke test: Login page
echo [9/10] Smoke test: Login page...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8080/login' -Method Get -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Login page not reachable.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Login page reachable.
    set /a PASS_COUNT+=1
)

:: 10. Smoke test: Registration page
echo [10/10] Smoke test: Registration page...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8080/register' -Method Get -UseBasicParsing -TimeoutSec 5; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }"
if %ERRORLEVEL% NEQ 0 (
    echo [FAIL] Registration page not reachable.
    set /a FAIL_COUNT+=1
) else (
    echo [PASS] Registration page reachable.
    set /a PASS_COUNT+=1
)

echo.
echo =====================================
echo  VERIFICATION SUMMARY
echo =====================================
echo  PASS: !PASS_COUNT!/10
echo  FAIL: !FAIL_COUNT!/10
echo =====================================

if !FAIL_COUNT! EQU 0 (
    echo  OVERALL: PASS
    exit /b 0
) else (
    echo  OVERALL: FAIL
    exit /b 1
)
