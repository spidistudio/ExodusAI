# ExodusAI - Offline AI Chat Android App

[![Android](https://img.shields.io/badge/Android-API%2021+-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![Android 16 QPR2](https://img.shields.io/badge/Android%2016%20QPR2-Compatible-red.svg?style=flat)](https://developer.android.com/about/versions)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Version](https://img.shields.io/badge/Version-1.14--QPR2-red.svg)](https://github.com/spidistudio/ExodusAI/releases)

ExodusAI is a cutting-edge Android application that allows you to chat with various AI models locally offline. The app provides a modern, customizable interface for interacting with local AI models like Ollama, now with full **Android 16 QPR2 developer preview support** - the most bleeding-edge Android compatibility available.

## 📱 Screenshots

*Coming soon - Screenshots will be added once the app is deployed*

## ✨ Features

- 🌙 **Adaptive Theme Design** - Dark/Light theme with system preference detection
- ⚙️ **Settings Screen** - Comprehensive settings with theme toggle and app info
- 💬 **Chat Interface** - Intuitive messaging interface similar to popular chat apps
- 🤖 **AI Model Selection** - Dropdown list to select from available downloaded AI models
- 🔄 **Model Updates** - Easy way to check for and download new AI models
- 💾 **Chat History** - Persistent storage of conversations with different models
- 📱 **Offline Operation** - Works entirely offline with local AI models
- 🚀 **Performance Optimized** - Built with modern Android architecture
- 🔧 **Android 16 Ready** - Full compatibility with Android 16 developer preview
- 📱 **Wide Compatibility** - Supports Android 5.0+ (API 21) to Android 16+

## 🆕 What's New in v1.14-QPR2

- **🚀 Android 16 QPR2 Compatibility**: Full support for Android 16 Quarterly Platform Release 2 (API 36)
- **🔒 Enhanced Security Compliance**: Updated for latest Android security requirements
- **⚙️ Settings Screen**: Dedicated settings page with theme switching functionality  
- **🎨 Theme Toggle**: Switch between dark and light modes instantly
- **📱 Bleeding-Edge Support**: First app to support Android 16 QPR2 developer builds
- **🛡️ Modern Security Model**: Compliance with experimental Android security features
- **🏗️ Advanced Build System**: Gradle 8.4 + Android Gradle Plugin 8.3.0
- **📊 App Information**: Version display and system information in settings

## ⚠️ **Android 16 QPR2 Notice**

This version specifically targets **Android 16 QPR2** (API 36), which is an experimental developer preview. If you're on:
- **Android 15 or older**: Use ExodusAI v1.11 or earlier  
- **Android 16 (non-QPR2)**: Use ExodusAI v1.12-v1.13
- **Android 16 QPR2**: Use this version (v1.14-QPR2) ✅

QPR2 builds may require special installation methods due to enhanced security.

## 🛠 Technical Stack

- **Language**: Kotlin 1.9.22
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt/Dagger 2.48
- **Database**: Room Database with SQLite
- **Networking**: Retrofit + OkHttp for API communication
- **Local AI**: Ollama integration (v1.0+)
- **Build System**: Gradle 8.4 + Android Gradle Plugin 8.3.0
- **Target SDK**: Android 16 QPR2 (API 36) with backwards compatibility to API 21
- **Security Model**: Enhanced compliance for experimental Android features
- **Theme System**: Material Design 3 with dynamic theming

## 📱 System Requirements

### Standard Android Devices
- **Android Version**: 5.0+ (API level 21)
- **RAM**: Minimum 2GB (4GB+ recommended for larger models)
- **Storage**: 100MB+ for app, additional space for AI models

### Experimental Android 16 QPR2 Support
- **✅ Android 16 QPR2**: Full compatibility with bleeding-edge developer builds
- **⚠️ Installation**: May require ADB or developer mode due to enhanced security
- **🔧 Developer Options**: Enable "Install unknown apps" and "USB debugging"
- **🛡️ Security**: QPR2 builds have stricter APK validation requirements

### Development Requirements
- **Java**: Java 18+ for development builds
- **Network**: WiFi connection to local Ollama server
- **Build Tools**: Android SDK with API 36 for QPR2 compatibility

## 🔧 Prerequisites

Before using the app, you need to have Ollama running on your device or local network:

1. **Install Ollama** on your computer or server
   - Visit [ollama.ai](https://ollama.ai) for installation instructions
   - Default runs on `http://localhost:11434`

2. **Download AI Models** in Ollama:
   ```bash
   ollama pull llama2
   ollama pull codellama
   ollama pull mistral
   ollama pull phi
   ```

3. **Configure Network Access**:
   - Default configuration: `http://192.168.0.115:11434`
   - For emulator: Use `http://10.0.2.2:11434`
   - For real device: Use your computer's IP address

## 📥 Installation

### Option 1: Download APK (Recommended)

**For Android 16 QPR2 Users:**
1. **Download APK v1.14-QPR2** from [Releases](https://github.com/spidistudio/ExodusAI/releases)
2. **Enable Developer Options**: Settings → About Phone → Tap "Build Number" 7 times
3. **Enable Installation Sources**: Settings → Developer Options → "Install unknown apps"
4. **Install via ADB** (most reliable for QPR2):
   ```bash
   adb install app-debug.apk
   ```

**For Standard Android Devices:**
1. **Download appropriate version** from [Releases](https://github.com/spidistudio/ExodusAI/releases)
   - Android 5.0-15: Use v1.11 or earlier
   - Android 16 (non-QPR2): Use v1.12-v1.13
2. **Enable Unknown Sources** in Android settings if needed
3. **Install APK** normally through file manager

### Option 2: Build from Source

**Requirements for QPR2 Builds:**
- Android Studio Hedgehog or later
- Java 18+ (OpenJDK recommended)
- Android SDK with API 36 (experimental)
- Gradle 8.4+ (handled by wrapper)

**Build Steps:**
1. **Clone the repository**:
   ```bash
   git clone https://github.com/spidistudio/ExodusAI.git
   cd ExodusAI
   ```

2. **Open in Android Studio**:
   - File → Open → Select project folder
   - Wait for Gradle sync (may show warnings about API 36)

3. **Configure for QPR2** (if building for Android 16 QPR2):
   - Ensure `compileSdk 36` and `targetSdk 36` in `app/build.gradle`
   - Verify `tools:targetApi="36"` in `AndroidManifest.xml`

4. **Build and Install**:
   ```bash
   # QPR2 compatible build
   ./gradlew assembleDebug
   
   # Install via ADB (recommended for QPR2)
   adb install app/build/outputs/apk/debug/app-debug.apk
   ```

## Configuration

### Ollama Setup

1. **Start Ollama service**:
   ```bash
   ollama serve
   ```

2. **Pull desired models**:
   ```bash
   ollama pull llama2:7b
   ollama pull codellama:7b
   ollama pull mistral:7b
   ```

3. **Verify models are available**:
   ```bash
   ollama list
   ```

### Network Configuration

If using a real Android device with Ollama on your computer:

1. Find your computer's IP address:
   ```bash
   # Windows
   ipconfig
   
   # macOS/Linux
   ifconfig
   ```

2. Update the base URL in `AppModule.kt`:
   ```kotlin
   .baseUrl("http://YOUR_COMPUTER_IP:11434/")
   ```

3. Ensure Ollama allows external connections:
   ```bash
   OLLAMA_HOST=0.0.0.0 ollama serve
   ```

## 🚀 Usage

1. **Launch ExodusAI** on your Android device

2. **Access Settings** (new in v1.12):
   - Tap the settings icon in the top-right corner
   - Choose between Dark/Light theme
   - View app version and system information

3. **Select an AI model** from the dropdown menu:
   - The app automatically detects available models from your Ollama instance
   - Models appear once Ollama connection is established

4. **Start chatting**:
   - Type your message in the input field at the bottom
   - Tap the send button (arrow icon)
   - Wait for the AI's response (usually 1-10 seconds depending on model)

5. **Manage models**:
   - Tap the refresh/update button to check for new models
   - Download new models through Ollama CLI

6. **Theme switching**:
   - Use the new Settings screen to toggle between dark and light modes
   - Theme preference is saved automatically

## 🎨 Customization

### Theme Options
- **Dark Mode**: Optimized for low-light usage (default)
- **Light Mode**: Clean, bright interface for daytime use
- **System Theme**: Automatically follows device theme setting (coming soon)

### Network Configuration
Default configuration works for most local setups. If needed, modify the base URL in `AppModule.kt`:

## 🏗 Project Structure

```
app/src/main/java/com/exodus/
├── data/
│   ├── api/              # Ollama API service definitions
│   ├── database/         # Room database and DAOs
│   ├── model/            # Data models and entities  
│   └── repository/       # Repository pattern implementation
├── di/                   # Hilt dependency injection modules
├── ui/
│   ├── chat/            # Chat screen and view model
│   ├── screens/         # Settings and other screens
│   ├── viewmodel/       # ViewModels for UI state management
│   └── theme/           # Material Design 3 theme configuration
├── MainActivity.kt       # Main activity with navigation
└── ExodusApplication.kt  # Application class with Hilt setup
```

## 🔗 API Integration

ExodusAI integrates with Ollama through its REST API:

- **GET** `/api/tags` - Fetch available models
- **POST** `/api/chat` - Send chat messages (streaming supported)
- **POST** `/api/pull` - Download new models
- **GET** `/api/version` - Check Ollama version

### Supported Ollama Models
- **LLaMA 2** (7B, 13B, 70B)
- **Code Llama** (7B, 13B, 34B)  
- **Mistral** (7B)
- **Phi** (1.5B, 3B)
- **Custom models** via Ollama Modelfile

## 💻 Development

### Building Different Variants

```bash
# Debug build (includes logging)
./gradlew assembleDebug

# Release build (optimized, signed)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest

# Clean build
./gradlew clean assembleDebug
```

### Development Environment Setup

1. **Java 18+**: Required for Android Gradle Plugin 8.3.0
2. **Android Studio**: Hedgehog (2023.1.1) or later recommended  
3. **Android SDK**: API 35 (Android 16 preview) required
4. **Gradle**: 8.4+ (handled automatically by wrapper)

### Architecture Patterns

**Clean Architecture Implementation:**
- **Presentation Layer**: Jetpack Compose UI + ViewModels
- **Domain Layer**: Use cases and business logic (Repository pattern)
- **Data Layer**: Room database + Retrofit networking

**Key Design Patterns:**
- Repository Pattern for data access
- Dependency Injection with Hilt
- MVVM for UI state management
- Observer Pattern for reactive data

### 🔧 Key Components

- **ChatRepository**: Handles communication with Ollama API and local database
- **ChatViewModel**: Manages chat UI state and business logic  
- **SettingsViewModel**: Manages settings state and theme switching
- **ChatScreen**: Main chat UI implemented with Jetpack Compose
- **SettingsScreen**: Settings interface with theme toggle (v1.12+)
- **ExodusDatabase**: Room database for persistent chat history
- **OllamaApiService**: Retrofit interface for Ollama REST API

## 🔍 Troubleshooting

### Android 16 QPR2 Specific Issues

1. **APK Installation Failures**:
   - ✅ Use **APK v1.14-QPR2** specifically designed for QPR2
   - ✅ Install via **ADB**: `adb install app-debug.apk`
   - ✅ Enable **Developer Options** and **USB debugging**
   - ✅ Check **"Install unknown apps"** for ADB/file manager
   - ✅ Temporarily disable **"Verify apps over USB"** in Developer Options

2. **Enhanced Security Restrictions**:
   - ✅ QPR2 has stricter APK validation - use correct API 36 build
   - ✅ Enable **"Allow from this source"** when prompted
   - ✅ Check **Security & Privacy → More security settings**
   - ✅ Some QPR2 builds require **manual permission grants**

3. **Parsing Package Errors**:
   - ✅ Ensure using **v1.14-QPR2** (not older versions)
   - ✅ Clear download cache and re-download APK
   - ✅ Try different transfer method (USB instead of WhatsApp)
   - ✅ Verify QPR2 build number in About Phone

### Common Issues (All Android Versions)

1. **"No models available"**:
   - ✅ Ensure Ollama is running: `ollama serve`
   - ✅ Check network connectivity (try: `ping 192.168.0.115`)
   - ✅ Verify models are downloaded: `ollama list`
   - ✅ Check app logs in Android Studio Logcat

2. **Connection timeout or network errors**:
   - ✅ Verify Ollama allows external connections: `OLLAMA_HOST=0.0.0.0 ollama serve`
   - ✅ Try different IP address in app configuration
   - ✅ Disable firewalls temporarily for testing
   - ✅ Check if port 11434 is accessible

3. **Theme not switching**:
   - ✅ Restart the app after changing theme in Settings
   - ✅ Clear app data if theme preference is stuck

4. **Models not loading after update**:
   - ✅ Refresh models list using the update button
   - ✅ Restart Ollama service: `ollama serve`
   - ✅ Check Ollama logs for errors

### Network Debugging

Enable detailed logging in debug builds:

1. **Check Android Logcat** for network errors:
   ```bash
   adb logcat -s ExodusAI
   ```

2. **Test Ollama connectivity** from terminal:
   ```bash
   # Test basic connectivity
   curl http://192.168.0.115:11434/api/tags
   
   # Test chat endpoint
   curl -X POST http://192.168.0.115:11434/api/chat \
        -H "Content-Type: application/json" \
        -d '{"model": "llama2", "messages": [{"role": "user", "content": "Hello"}]}'
   ```

3. **Common network fixes**:
   - Restart WiFi on both devices
   - Use `ipconfig`/`ifconfig` to verify IP addresses
   - Check router settings for device isolation

## 🤝 Contributing

We welcome contributions! ExodusAI is open-source and community-driven.

### How to Contribute

1. **Fork the repository** on GitHub
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Make your changes** and test thoroughly
4. **Follow coding standards**: Kotlin style guide, proper documentation
5. **Write tests** if applicable (unit tests preferred)
6. **Commit changes**: `git commit -m 'Add amazing feature'`
7. **Push to branch**: `git push origin feature/amazing-feature`
8. **Open a Pull Request** with detailed description

### Development Guidelines

- **Code Style**: Follow Kotlin coding conventions
- **Architecture**: Maintain Clean Architecture principles  
- **Testing**: Add unit tests for new functionality
- **Documentation**: Update README for significant changes
- **Android 16**: Test compatibility with latest Android versions

### Areas for Contribution

- 🐛 Bug fixes and stability improvements
- 🎨 UI/UX enhancements and themes
- 🚀 Performance optimizations
- 📱 New Android version compatibility
- 🌐 Internationalization (i18n)
- 🔧 Additional Ollama features integration
- 📖 Documentation improvements

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for full details.

```
MIT License - You are free to:
✅ Use commercially
✅ Modify and distribute  
✅ Private use
✅ Include in larger projects

❗ Conditions: Include license and copyright notice
```

## 🙏 Acknowledgments

- **[Ollama](https://ollama.ai)** - For providing excellent local AI infrastructure
- **[Material Design](https://material.io)** - For comprehensive design guidelines  
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** - For modern declarative UI
- **[Android Team](https://developer.android.com)** - For Android 16 developer preview
- **Open Source Community** - For tools, libraries, and inspiration

## 📞 Support & Community

### Get Help
- 📖 **Documentation**: Check this README and code comments
- 🐛 **Bug Reports**: [Create GitHub Issue](https://github.com/spidistudio/ExodusAI/issues)
- 💡 **Feature Requests**: [GitHub Discussions](https://github.com/spidistudio/ExodusAI/discussions)
- ❓ **Questions**: Use GitHub Discussions for general questions

### Supported Platforms
- ✅ **Android 5.0 - 15**: Fully supported and tested (use v1.11 or earlier)
- ✅ **Android 16 Preview**: Compatible (use v1.12-v1.13)
- ✅ **Android 16 QPR2**: Cutting-edge support (use v1.14-QPR2) 🔥
- ✅ **Emulators**: Android Studio AVD, Genymotion
- ✅ **Real Devices**: All major manufacturers

---

## 📈 Roadmap

### v1.15 (Coming Soon)
- [ ] Android 17 early compatibility
- [ ] System theme detection (auto dark/light)
- [ ] Enhanced QPR2 security compliance
- [ ] Performance optimizations for experimental builds

### Future Plans
- [ ] Multi-language support (i18n)
- [ ] Voice input/output integration
- [ ] Custom model configuration UI
- [ ] Export/import chat history
- [ ] Advanced theming options
- [ ] Edge-to-edge display optimization

---

**⚠️ Important Notes for Android 16 QPR2:**
- QPR2 is an experimental developer preview with enhanced security
- Installation may require ADB or special developer permissions
- Some features may behave differently on experimental builds
- Report QPR2-specific issues on GitHub for rapid fixes

**🚀 Ready for bleeding-edge Android 16 QPR2? [Download v1.14-QPR2](https://github.com/spidistudio/ExodusAI/releases) and experience the future!**
