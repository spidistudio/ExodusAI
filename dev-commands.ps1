# ExodusAI Development Commands

# Build debug APK
function Build-Debug {
    Write-Host "Building ExodusAI Debug APK..." -ForegroundColor Green
    .\gradlew.bat assembleDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Build successful!" -ForegroundColor Green
        Write-Host "APK location: app\build\outputs\apk\debug\app-debug.apk"
    } else {
        Write-Host "❌ Build failed!" -ForegroundColor Red
    }
}

# Install on device/emulator
function Install-App {
    Write-Host "Installing ExodusAI on device..." -ForegroundColor Green
    .\gradlew.bat installDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ App installed successfully!" -ForegroundColor Green
    } else {
        Write-Host "❌ Installation failed!" -ForegroundColor Red
    }
}

# Clean project
function Clean-Project {
    Write-Host "Cleaning ExodusAI project..." -ForegroundColor Yellow
    .\gradlew.bat clean
    Write-Host "✅ Project cleaned!" -ForegroundColor Green
}

# Start emulator
function Start-Emulator {
    Write-Host "Starting Android Emulator..." -ForegroundColor Blue
    Start-Process "emulator" -ArgumentList "-avd", "ExodusAI_Test" -NoNewWindow
    Write-Host "✅ Emulator starting..." -ForegroundColor Green
}

# View logs
function View-Logs {
    Write-Host "Viewing ExodusAI logs..." -ForegroundColor Cyan
    adb logcat | Select-String "ExodusAI"
}

# Open preview
function Open-Preview {
    Write-Host "Opening ExodusAI preview..." -ForegroundColor Magenta
    Start-Process "preview\android-preview.html"
    Write-Host "✅ Preview opened!" -ForegroundColor Green
}

# Show available commands
function Show-Commands {
    Write-Host ""
    Write-Host "🚀 ExodusAI Development Commands:" -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Build-Debug     - Build debug APK" -ForegroundColor Cyan
    Write-Host "Install-App     - Install app on device/emulator" -ForegroundColor Cyan
    Write-Host "Clean-Project   - Clean the project" -ForegroundColor Cyan
    Write-Host "Start-Emulator  - Start Android emulator" -ForegroundColor Cyan
    Write-Host "View-Logs       - View app logs" -ForegroundColor Cyan
    Write-Host "Open-Preview    - Open web preview" -ForegroundColor Cyan
    Write-Host "Show-Commands   - Show this help" -ForegroundColor Cyan
    Write-Host ""
    Write-Host "Usage: Just type the command name in PowerShell" -ForegroundColor Green
    Write-Host "Example: Build-Debug" -ForegroundColor Green
    Write-Host ""
}

Write-Host "ExodusAI Development Environment Loaded! 🎉" -ForegroundColor Green
Write-Host "Type 'Show-Commands' to see available commands." -ForegroundColor Cyan