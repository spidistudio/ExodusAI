@echo off
echo Starting ExodusAI Development Environment...

echo.
echo =================================
echo  ExodusAI Android Development Menu
echo =================================
echo.
echo 1. Build Debug APK
echo 2. Install on Device/Emulator  
echo 3. Clean Project
echo 4. Start Android Emulator
echo 5. View Build Logs
echo 6. Open Android Preview in Browser
echo 7. Exit
echo.

:menu
set /p choice="Enter your choice (1-7): "

if "%choice%"=="1" goto build
if "%choice%"=="2" goto install  
if "%choice%"=="3" goto clean
if "%choice%"=="4" goto emulator
if "%choice%"=="5" goto logs
if "%choice%"=="6" goto preview
if "%choice%"=="7" goto exit

echo Invalid choice. Please try again.
goto menu

:build
echo Building Debug APK...
call gradlew.bat assembleDebug
if %errorlevel% equ 0 (
    echo.
    echo ✅ Build successful! APK created at: app\build\outputs\apk\debug\app-debug.apk
) else (
    echo.
    echo ❌ Build failed. Check the errors above.
)
echo.
pause
goto menu

:install
echo Installing APK on device/emulator...
call gradlew.bat installDebug
if %errorlevel% equ 0 (
    echo.
    echo ✅ App installed successfully!
) else (
    echo.
    echo ❌ Installation failed. Make sure device is connected or emulator is running.
)
echo.
pause
goto menu

:clean
echo Cleaning project...
call gradlew.bat clean
echo ✅ Project cleaned!
echo.
pause
goto menu

:emulator
echo Starting Android Emulator...
echo Note: Make sure you have created an AVD first using Android Studio or avdmanager
echo.
start /b emulator -avd ExodusAI_Test
echo ✅ Emulator starting in background...
echo.
pause
goto menu

:logs
echo Viewing build logs...
adb logcat | findstr "ExodusAI"
pause
goto menu

:preview
echo Opening Android Preview in browser...
start "" "preview\android-preview.html"
echo ✅ Preview opened in default browser
echo.
pause
goto menu

:exit
echo.
echo Thanks for developing ExodusAI! 🚀
echo.
pause
exit
