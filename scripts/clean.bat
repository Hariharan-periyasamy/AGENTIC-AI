@echo off
setlocal EnableDelayedExpansion
cd /d "%~dp0.."

echo =====================================
echo  CLEANING WORKSPACE
echo =====================================

echo Stopping containers if running...
docker compose down >nul 2>&1

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=C:\apache-maven-3.9.16\bin\mvn.cmd"
    )
)

echo Removing Maven build artifacts...
call %MVN_CMD% clean >nul 2>&1
echo [OK] Build artifacts removed.

echo.
set /p confirmVolume="Do you want to permanently delete the database volumes? (This destroys ALL user data!) [y/N]: "
if /i "%confirmVolume%"=="y" (
    docker compose down -v
    echo [OK] Database volumes destroyed.
) else (
    echo [OK] Database volumes preserved.
)

echo.
set /p confirmUploads="Do you want to delete local upload/certificate files? [y/N]: "
if /i "%confirmUploads%"=="y" (
    if exist "uploads\" rmdir /s /q "uploads"
    echo [OK] Uploads directory deleted.
) else (
    echo [OK] Uploads preserved.
)

echo.
echo [SUCCESS] Workspace cleaned.
exit /b 0
