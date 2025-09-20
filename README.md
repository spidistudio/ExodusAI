# ExodusAI - Offline AI Chat Android App

[![Android](https://img.shields.io/badge/Android-API%2024+-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=24)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9+-blue.svg?style=flat)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

ExodusAI is a modern Android application that allows you to chat with various AI models locally offline. The app provides a simple, dark-themed interface for interacting with local AI models like Ollama.

## 📱 Screenshots

*Coming soon - Screenshots will be added once the app is deployed*

## Features

- 🌙 **Dark Theme Design** - Clean, modern UI optimized for low-light usage
- 💬 **Chat Interface** - Intuitive messaging interface similar to popular chat apps
- 🤖 **AI Model Selection** - Dropdown list to select from available downloaded AI models
- 🔄 **Model Updates** - Easy way to check for and download new AI models
- 💾 **Chat History** - Persistent storage of conversations with different models
- 📱 **Offline Operation** - Works entirely offline with local AI models
- 🚀 **Performance Optimized** - Built with modern Android architecture

## Technical Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Clean Architecture
- **Dependency Injection**: Hilt
- **Database**: Room
- **Networking**: Retrofit + OkHttp
- **Local AI**: Ollama integration
- **Material Design**: Material Design 3

## Prerequisites

Before using the app, you need to have Ollama running on your device or local network:

1. **Install Ollama** on your computer or server
   - Visit [ollama.ai](https://ollama.ai) for installation instructions
   - Default runs on `http://localhost:11434`

2. **Download AI Models** in Ollama:
   ```bash
   ollama pull llama2
   ollama pull codellama
   ollama pull mistral
   ```

3. **Configure Network Access** (if needed):
   - For emulator: Use `http://10.0.2.2:11434`
   - For real device: Use your computer's IP address

## Installation

### Option 1: Build from Source

1. **Clone the repository**:
   ```bash
   git clone <repository-url>
   cd ExodusAI
   ```

2. **Open in Android Studio**:
   - Install Android Studio Arctic Fox or later
   - Open the project
   - Sync Gradle files

3. **Configure Ollama URL** (if needed):
   - Edit `di/AppModule.kt`
   - Update the base URL in `provideRetrofit()` method

4. **Build and Run**:
   - Connect an Android device or start an emulator
   - Click "Run" or use `./gradlew installDebug`

### Option 2: Install APK

Download the latest APK from the releases section and install on your Android device.

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

## Usage

1. **Launch the app** on your Android device

2. **Select an AI model** from the dropdown menu
   - The app will automatically detect available models from your Ollama instance

3. **Start chatting**:
   - Type your message in the input field
   - Tap the send button
   - Wait for the AI's response

4. **Update models**:
   - Tap the refresh button to check for new models
   - The app will update the available models list

5. **Clear chat**:
   - Use the clear button to remove chat history
   - History is cleared per model

## Project Structure

```
app/src/main/java/com/wikiai/
├── data/
│   ├── api/           # Ollama API service definitions
│   ├── database/      # Room database and DAOs
│   ├── model/         # Data models and entities
│   └── repository/    # Repository pattern implementation
├── di/                # Dependency injection modules
├── ui/
│   ├── chat/          # Chat screen and view model
│   └── theme/         # Material Design theme
├── MainActivity.kt    # Main activity
└── ExodusApplication.kt # Application class
```

## API Integration

The app integrates with Ollama through its REST API:

- **GET** `/api/tags` - Fetch available models
- **POST** `/api/chat` - Send chat messages
- **POST** `/api/pull` - Download new models

## Development

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build
./gradlew assembleRelease

# Run tests
./gradlew test
```

### Architecture

The app follows Clean Architecture principles:

- **Presentation Layer**: Jetpack Compose UI + ViewModels
- **Domain Layer**: Use cases and business logic
- **Data Layer**: Repository pattern with Room + Retrofit

### Key Components

- **ChatRepository**: Handles communication with Ollama API and local database
- **ChatViewModel**: Manages UI state and business logic
- **ChatScreen**: Main UI implemented with Jetpack Compose
- **ExodusAIDatabase**: Room database for chat history

## Troubleshooting

### Common Issues

1. **"No models available"**:
   - Ensure Ollama is running
   - Check network connectivity
   - Verify the base URL is correct

2. **Connection timeout**:
   - Check if Ollama is accessible from your device
   - Try using your computer's IP address instead of localhost

3. **Models not loading**:
   - Make sure you've downloaded models in Ollama
   - Restart the Ollama service

4. **App crashes on startup**:
   - Check if all dependencies are properly configured
   - Clear app data and try again

### Network Debugging

To debug network issues:

1. Enable logging in the app (already enabled in debug builds)
2. Check Android Studio's Logcat for network errors
3. Test Ollama connectivity:
   ```bash
   curl http://YOUR_OLLAMA_URL/api/tags
   ```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Write tests if applicable
5. Submit a pull request

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request. For major changes, please open an issue first to discuss what you would like to change.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- [Ollama](https://ollama.ai) for providing the local AI infrastructure
- [Material Design](https://material.io) for design guidelines
- [Jetpack Compose](https://developer.android.com/jetpack/compose) for modern UI development

## 📞 Support

For issues and questions:
1. Check the troubleshooting section
2. Search existing issues on GitHub
3. Create a new issue with detailed information

---

**Note**: This app requires a local Ollama installation to function. It does not include AI models and relies on external services for AI functionality.
