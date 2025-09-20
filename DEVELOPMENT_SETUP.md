# ExodusAI Development Setup for VS Code

## VS Code Android Development Setup

### Prerequisites
1. **Java Development Kit (JDK) 11 or higher**
2. **Android SDK Command Line Tools**
3. **VS Code Extensions** (already installed)

### Setup Instructions

#### 1. Install Android SDK
```powershell
# Download Android SDK Command Line Tools
# Extract to: C:\Users\%USERNAME%\AppData\Local\Android\Sdk

# Set environment variables
$env:ANDROID_HOME = "C:\Users\$env:USERNAME\AppData\Local\Android\Sdk"
$env:PATH += ";$env:ANDROID_HOME\cmdline-tools\latest\bin"
$env:PATH += ";$env:ANDROID_HOME\platform-tools"
$env:PATH += ";$env:ANDROID_HOME\emulator"
```

#### 2. Accept SDK Licenses
```powershell
sdkmanager --licenses
```

#### 3. Install Required SDK Components
```powershell
sdkmanager "platform-tools" "platforms;android-34" "build-tools;34.0.0"
sdkmanager "system-images;android-34;google_apis;x86_64"
```

#### 4. Create Virtual Device (AVD)
```powershell
avdmanager create avd -n "ExodusAI_Test" -k "system-images;android-34;google_apis;x86_64"
```

#### 5. Build and Run
```powershell
# Build the app
./gradlew assembleDebug

# Install on device/emulator
./gradlew installDebug

# Start emulator
emulator -avd ExodusAI_Test
```

### VS Code Tasks
- **Build**: Ctrl+Shift+P → "Tasks: Run Task" → "Build ExodusAI Android App"
- **Clean**: `./gradlew clean`
- **Debug Build**: `./gradlew assembleDebug`

### Alternative IDEs
1. **IntelliJ IDEA Community** (Free, similar to Android Studio)
2. **VS Code** with Android extensions
3. **Vim/Neovim** with LSP support
4. **Sublime Text** with Kotlin package
