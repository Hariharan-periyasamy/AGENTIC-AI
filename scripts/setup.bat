@echo off
setlocal EnableDelayedExpansion

:: Change to project root directory reliably
cd /d "%~dp0.."

echo =====================================
echo  DIGITAL CERTIFICATE SYSTEM
echo  SETUP AND PREREQUISITE CHECK
echo =====================================
echo.

echo Checking prerequisites...

:: 1. Check Java
where java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Java is not installed or not in PATH.
    echo Please install Java 21 from https://adoptium.net/ and ensure it is in your system PATH.
    exit /b 1
)
java -version 2>&1 | findstr /i "version" | findstr "21" >nul
if %ERRORLEVEL% NEQ 0 (
    echo [WARNING] Java version 21 is strongly recommended. You may face compilation issues.
) else (
    echo [OK] Java 21 found.
)

:: 2. Check Maven
set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    if exist "C:\apache-maven-3.9.16\bin\mvn.cmd" (
        set "MVN_CMD=C:\apache-maven-3.9.16\bin\mvn.cmd"
    )
)
call %MVN_CMD% -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven is not installed or not in PATH (checked '%MVN_CMD%').
    echo Please install Maven 3.9+ from https://maven.apache.org/download.cgi and add it to your PATH.
    exit /b 1
)
echo [OK] Maven found.

:: 3. Check Git
where git >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Git is not installed or not in PATH.
    echo Please install Git from https://git-scm.com/
    exit /b 1
)
echo [OK] Git found.

:: 4. Check Docker
where docker >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker is not installed or not in PATH.
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    exit /b 1
)
echo [OK] Docker found.

:: 5. Check Docker Compose
docker compose version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Docker Compose plugin is not installed or Docker daemon is not running.
    echo Please ensure Docker Desktop is running.
    exit /b 1
)
echo [OK] Docker Compose found.

echo.
echo Preparing required directories...
if not exist "uploads\certificates" (
    mkdir "uploads\certificates"
    echo [OK] Created uploads\certificates directory.
) else (
    echo [OK] Directories already exist.
)

echo.
echo Building project...
call %MVN_CMD% clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Maven build failed.
    exit /b %ERRORLEVEL%
)

echo.
echo Running initial unit tests...
call %MVN_CMD% test
if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Initial tests failed.
    exit /b %ERRORLEVEL%
)

echo.
echo =====================================
echo  SETUP COMPLETE
echo =====================================
exit /b 0
