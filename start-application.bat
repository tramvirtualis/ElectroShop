@echo off
echo Starting ElectroShop Application...
echo.

REM Navigate to project directory
cd /d "D:\Y4S1\LapTrinhWeb\ElectroShop"

REM Clean and compile
echo Cleaning and compiling project...
call mvn clean compile

REM Start the application
echo Starting Spring Boot application...
echo.
echo Application will be available at: http://localhost:8080
echo Database test endpoint: http://localhost:8080/api/test/database
echo Health check endpoint: http://localhost:8080/api/test/health
echo.
echo Press Ctrl+C to stop the application
echo.

call mvn spring-boot:run

pause


