@echo off
setlocal
echo ============================================
echo   ROCKET ON TOP - Android APK Builder
echo ============================================
where gradle >nul 2>nul
if errorlevel 1 (
  echo Gradle was not found. Install Android Studio or Gradle and Android SDK.
  echo Then reopen this file from the project folder.
  pause
  exit /b 1
)
gradle --no-daemon assembleDebug
if errorlevel 1 (
  echo APK build failed. Check Android SDK / Gradle setup.
  pause
  exit /b 1
)
echo.
echo APK created at:
echo app\build\outputs\apk\debug\app-debug.apk
pause
