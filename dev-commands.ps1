# ExodusAI Development Commands

# Build debug APK
build-debug() {
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
install-app() {
    Write-Host "Installing ExodusAI on device..." -ForegroundColor Green
    .\gradlew.bat installDebug
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ App installed successfully!" -ForegroundColor Green
    } else {
        Write-Host "❌ Installation failed!" -ForegroundColor Red
    }
}

# Clean project
clean-project() {
    Write-Host "Cleaning ExodusAI project..." -ForegroundColor Yellow
    .\gradlew.bat clean
    Write-Host "✅ Project cleaned!" -ForegroundColor Green
}

# Start emulator
start-emulator() {
    Write-Host "Starting Android Emulator..." -ForegroundColor Blue
    Start-Process "emulator" -ArgumentList "-avd", "ExodusAI_Test" -NoNewWindow
    Write-Host "✅ Emulator starting..." -ForegroundColor Green
}

# View logs
view-logs() {
    Write-Host "Viewing ExodusAI logs..." -ForegroundColor Cyan
    adb logcat | Select-String "ExodusAI"
}

# Open preview
open-preview() {
    Write-Host "Opening ExodusAI preview..." -ForegroundColor Magenta
    Start-Process "preview\android-preview.html"
    Write-Host "✅ Preview opened!" -ForegroundColor Green
}

# Show available commands
Show-Commands() {
    Write-Host @"

🚀 ExodusAI Development Commands:

build-debug     - Build debug APK
install-app     - Install app on device/emulator
clean-project   - Clean the project
start-emulator  - Start Android emulator
view-logs       - View app logs
open-preview    - Open web preview
Show-Commands   - Show this help

Usage: Just type the command name in PowerShell
Example: build-debug

"@ -ForegroundColor Yellow
}

Write-Host "ExodusAI Development Environment Loaded! 🎉" -ForegroundColor Green
Write-Host "Type 'Show-Commands' to see available commands." -ForegroundColor Cyan
