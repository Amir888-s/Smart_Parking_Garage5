@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
if not exist out mkdir out
set "SOURCES="
for /r "src\main\java" %%F in (*.java) do set "SOURCES=!SOURCES! "%%F""
echo Compiling Smart Parking Garage...
call javac -encoding UTF-8 -d out !SOURCES!
if errorlevel 1 (
  echo Compilation failed. Check that a JDK 17 or newer is installed.
  pause
  exit /b 1
)
echo.
echo Starting demo...
java -cp out edu.aitu.parking.app.SmartParkingDemo
pause
