@echo off
echo Running ElectroShop Tests...
echo.

REM Navigate to project directory
cd /d "D:\Y4S1\LapTrinhWeb\ElectroShop"

REM Run simple unit tests first
echo Running simple unit tests...
call mvn test -Dtest=SimpleHomeControllerTest

echo.
echo Running all tests...
call mvn test

echo.
echo Test execution completed.
pause


