@echo off
REM AutoEscalate Ops Setup Script for Windows
REM This script sets up and runs the application

echo ==================================
echo AutoEscalate Ops - Setup Script
echo ==================================
echo.

REM Check Java version
echo Checking Java version...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Java is not installed. Please install Java 21 or higher.
    pause
    exit /b 1
)
echo √ Java detected
echo.

REM Check Maven
echo Checking Maven...
mvn -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo X Maven is not installed. Please install Maven 3.9 or higher.
    pause
    exit /b 1
)
echo √ Maven detected
echo.

REM Check Node.js (optional)
echo Checking Node.js...
node -v >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo ! Node.js is not installed. Some features may not work.
) else (
    echo √ Node.js detected
)
echo.

REM Install npm dependencies (if needed)
if exist package.json (
    echo Installing npm dependencies...
    call npm install
    echo √ npm dependencies installed
    echo.
)

REM Build the backend
echo Building backend application...
cd srv
call mvn clean install -DskipTests
if %ERRORLEVEL% NEQ 0 (
    echo X Build failed
    pause
    exit /b 1
)
echo √ Backend built successfully
echo.

REM Start the application
echo ==================================
echo Starting AutoEscalate Ops...
echo ==================================
echo.
echo Frontend will be available at:
echo   http://localhost:8080/issuetracker/webapp/index.html
echo.
echo API will be available at:
echo   http://localhost:8080/api/issues
echo.
echo Login credentials:
echo   Username: authenticated
echo   Password: (leave blank)
echo.
echo Press Ctrl+C to stop the application
echo.

REM Run the application
call mvn spring-boot:run
