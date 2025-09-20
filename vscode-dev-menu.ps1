# ExodusAI Development Menu for VS Code

Write-Host "==================================" -ForegroundColor Cyan
Write-Host "    ExodusAI Android Development   " -ForegroundColor Yellow
Write-Host "         VS Code Edition          " -ForegroundColor Yellow
Write-Host "==================================" -ForegroundColor Cyan
Write-Host ""

function Show-Menu {
    Write-Host "Available Commands:" -ForegroundColor Green
    Write-Host ""
    Write-Host "[1] Build App (Debug)" -ForegroundColor White
    Write-Host "[2] Clean Project" -ForegroundColor White
    Write-Host "[3] Start Emulator" -ForegroundColor White
    Write-Host "[4] Install App to Device" -ForegroundColor White
    Write-Host "[5] View App Logs" -ForegroundColor White
    Write-Host "[6] List Connected Devices" -ForegroundColor White
    Write-Host "[7] Open VS Code" -ForegroundColor White
    Write-Host "[8] Git Status" -ForegroundColor White
    Write-Host "[Q] Quit" -ForegroundColor Red
    Write-Host ""
}

function Execute-Choice {
    param([string]$choice)
    
    switch ($choice.ToUpper()) {
        "1" {
            Write-Host "Building ExodusAI app..." -ForegroundColor Yellow
            .\gradlew.bat assembleDebug
        }
        "2" {
            Write-Host "Cleaning project..." -ForegroundColor Yellow
            .\gradlew.bat clean
        }
        "3" {
            Write-Host "Starting Android emulator..." -ForegroundColor Yellow
            Start-Process "C:\Users\Milan\AppData\Local\Android\Sdk\emulator\emulator.exe" -ArgumentList "-avd", "Medium_Phone_API_36.1"
        }
        "4" {
            Write-Host "Installing app to device..." -ForegroundColor Yellow
            .\gradlew.bat installDebug
        }
        "5" {
            Write-Host "Viewing app logs (Ctrl+C to stop)..." -ForegroundColor Yellow
            & "C:\Users\Milan\AppData\Local\Android\Sdk\platform-tools\adb.exe" logcat -s ExodusAI
        }
        "6" {
            Write-Host "Connected devices:" -ForegroundColor Yellow
            & "C:\Users\Milan\AppData\Local\Android\Sdk\platform-tools\adb.exe" devices
        }
        "7" {
            Write-Host "Opening VS Code..." -ForegroundColor Yellow
            code .
        }
        "8" {
            Write-Host "Git status:" -ForegroundColor Yellow
            git status
        }
        "Q" {
            Write-Host "Goodbye!" -ForegroundColor Green
            exit
        }
        default {
            Write-Host "Invalid choice. Please try again." -ForegroundColor Red
        }
    }
}

# Main menu loop
do {
    Show-Menu
    $choice = Read-Host "Enter your choice"
    Write-Host ""
    Execute-Choice $choice
    Write-Host ""
    if ($choice.ToUpper() -ne "Q") {
        Write-Host "Press any key to continue..." -ForegroundColor Gray
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        Clear-Host
        Write-Host "==================================" -ForegroundColor Cyan
        Write-Host "    ExodusAI Android Development   " -ForegroundColor Yellow
        Write-Host "         VS Code Edition          " -ForegroundColor Yellow
        Write-Host "==================================" -ForegroundColor Cyan
        Write-Host ""
    }
} while ($choice.ToUpper() -ne "Q")