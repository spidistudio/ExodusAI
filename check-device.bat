@echo off
echo ===========================================
echo    ExodusAI Device Connection Test
echo ===========================================
echo.
echo Checking for connected Android devices...
echo.
C:\Users\Milan\AppData\Local\Android\Sdk\platform-tools\adb.exe devices -l
echo.
if errorlevel 1 (
    echo ERROR: ADB not working properly
    pause
    exit /b 1
)

echo.
echo ========================================
echo If your device shows above, you can run:
echo   gradlew.bat installDebug
echo.
echo If no device shows:
echo 1. Enable Developer Options on phone
echo 2. Enable USB Debugging
echo 3. Connect USB cable
echo 4. Allow debugging popup
echo ========================================
echo.
pause