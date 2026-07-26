@echo off
setlocal enabledelayedexpansion

where gradle >nul 2>nul
if %errorlevel%==0 (
  gradle %*
  exit /b %errorlevel%
)

echo Gradle is not installed on this Windows machine.
echo Open this project in Android Studio or install Gradle, then run: gradle %*
exit /b 1
