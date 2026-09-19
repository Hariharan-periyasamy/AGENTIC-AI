@echo off
cd /d "%~dp0.."

echo =====================================
echo  RUNNING MAVEN TESTS
echo =====================================

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=C:\apache-maven-3.9.16\bin\mvn.cmd"
    )
)

call %MVN_CMD% test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven tests failed!
    exit /b %ERRORLEVEL%
)

echo [OK] All tests passed! Test reports generated in target/surefire-reports.
exit /b 0
