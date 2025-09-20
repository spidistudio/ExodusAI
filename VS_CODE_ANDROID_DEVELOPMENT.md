# VS Code Android Development Guide for ExodusAI

## Overview
You can now develop your ExodusAI Android app entirely within VS Code! This guide shows you how to build, test, and debug your app without needing Android Studio.

## Prerequisites ✅
- VS Code with Android development extensions installed
- Android SDK configured at: `C:/Users/Milan/AppData/Local/Android/Sdk`
- Java 18+ runtime environment
- Gradle wrapper configured

## Development Workflow

### 1. Building the App
Use the VS Code Command Palette (`Ctrl+Shift+P`):
- Type: `Tasks: Run Task`
- Select: `Build ExodusAI Android App`

Or use the terminal:
```powershell
./gradlew.bat assembleDebug
```

### 2. Starting the Android Emulator
Use VS Code Command Palette:
- Type: `Tasks: Run Task`
- Select: `Start Android Emulator`

This will start your existing `Medium_Phone_API_36.1` emulator.

### 3. Installing the App
Once the emulator is running:
- Type: `Tasks: Run Task`
- Select: `Install ExodusAI on Device`

### 4. Viewing Logs
To see app logs:
- Type: `Tasks: Run Task`
- Select: `View Android Logs`

### 5. Debugging
1. Build and install your app first
2. Start the emulator
3. Open your app on the emulator
4. Use VS Code's debug panel (`F5`) to attach debugger

## Available VS Code Tasks

| Task | Description |
|------|-------------|
| Build ExodusAI Android App | Compiles the debug APK |
| Install ExodusAI on Device | Installs the app on connected device/emulator |
| Clean ExodusAI Project | Cleans build artifacts |
| Start Android Emulator | Starts the Android emulator |
| List Android Devices | Shows connected devices |
| View Android Logs | Shows app logs in real-time |

## File Structure
```
ExodusAI/
├── app/
│   ├── src/main/
│   │   ├── java/com/exodus/
│   │   │   ├── MainActivity.kt
│   │   │   ├── ExodusApplication.kt
│   │   │   ├── data/ (API, Database, Models)
│   │   │   ├── di/ (Dependency Injection)
│   │   │   └── ui/ (Compose UI, ViewModels)
│   │   └── AndroidManifest.xml
│   └── build.gradle (App-level config)
├── gradle.properties (Project config)
├── build.gradle (Project-level config)
└── .vscode/ (VS Code configuration)
```

## Key Features Implemented
- 🔥 Material Design 3 with dark theme
- 🏗️ Clean Architecture + MVVM
- 🎨 Jetpack Compose UI
- 🗄️ Room database for chat history
- 💉 Hilt dependency injection
- 🤖 Ollama AI integration (offline)
- 📱 Modern Android development practices

## Testing Your Changes
1. Make code changes in VS Code
2. Build: `Ctrl+Shift+P` → `Tasks: Run Task` → `Build ExodusAI Android App`
3. Install: `Tasks: Run Task` → `Install ExodusAI on Device`
4. Test on emulator

## Troubleshooting

### Build Issues
- **Gradle errors**: Run `Tasks: Run Task` → `Clean ExodusAI Project`
- **Java issues**: Check that JAVA_HOME points to Java 18+
- **SDK issues**: Verify Android SDK path in local.properties

### Emulator Issues
- **Won't start**: Check if HAXM/Hyper-V is properly configured
- **Slow performance**: Allocate more RAM to emulator
- **Connection issues**: Use `Tasks: Run Task` → `List Android Devices`

### App Issues
- **Installation fails**: Check device compatibility (API 24+)
- **Crashes**: Use `Tasks: Run Task` → `View Android Logs`
- **Performance**: Use VS Code profiler tools

## Next Steps
1. Start developing new features in the `app/src/main/java/com/exodus/` directory
2. Use Jetpack Compose for UI components
3. Test frequently on the emulator
4. Use Git for version control

## Quick Development Commands
```powershell
# Build the app
./gradlew.bat assembleDebug

# Clean build
./gradlew.bat clean

# Install to device
./gradlew.bat installDebug

# Check devices
C:\Users\Milan\AppData\Local\Android\Sdk\platform-tools\adb.exe devices

# View logs
C:\Users\Milan\AppData\Local\Android\Sdk\platform-tools\adb.exe logcat -s ExodusAI
```

---
*Happy coding! 🚀 Your Android development environment is fully configured for VS Code.*