@echo off
REM EventMate Auth Service - Build and Verification Script
REM This script verifies the project structure and builds the application

echo ====================================
echo EventMate Auth Service Verification
echo ====================================
echo.

echo [1/5] Checking Java version...
java -version
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Java not found or not in PATH
    exit /b 1
)
echo.

echo [2/5] Checking project structure...
if not exist "src\main\java\com\eventmate\auth\AuthApplication.java" (
    echo ERROR: AuthApplication.java not found
    exit /b 1
)
if not exist "build.gradle" (
    echo ERROR: build.gradle not found
    exit /b 1
)
if not exist "src\main\resources\application.yml" (
    echo ERROR: application.yml not found
    exit /b 1
)
echo Project structure: OK
echo.

echo [3/5] Listing all Java source files...
dir /s /b src\main\java\*.java | find /c ".java"
echo source files found.
echo.

echo [4/5] Building the project (without tests)...
call gradlew.bat clean build -x test --no-daemon
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Build failed
    exit /b 1
)
echo Build: SUCCESS
echo.

echo [5/5] Checking build artifacts...
if exist "build\libs\auth-*.jar" (
    echo JAR file created successfully:
    dir build\libs\*.jar
) else (
    echo WARNING: JAR file not found in build\libs
)
echo.

echo ====================================
echo Verification Complete!
echo ====================================
echo.
echo Next steps:
echo 1. Setup PostgreSQL database
echo 2. Run: psql -U postgres -d eventmate-auth -f database-setup.sql
echo 3. Set environment variables (optional):
echo    set DB_PASSWORD=your_password
echo    set JWT_SECRET=your_secret
echo 4. Run the application:
echo    gradlew.bat bootRun
echo.
echo For more information, see:
echo - README.md (complete documentation)
echo - QUICKSTART.md (getting started guide)
echo - IMPLEMENTATION-SUMMARY.md (what was built)
echo.

pause

