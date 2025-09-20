# Changelog

All notable changes to ExodusAI will be documented in this file.

## [1.04] - 2025-09-20

### Added
- Single model UI design with CodeLlama 13B Instruct as default model
- Proper StateFlow-based reactive state management
- Comprehensive demo mode with helpful setup instructions
- Network configuration for connecting to Ollama server over WiFi
- Better error handling with detailed fallback messages
- CodeLlama-specific UI and messaging optimized for coding tasks

### Fixed
- Messages no longer disappear when sent
- ChatViewModel now properly manages state updates using Android ViewModel
- UI correctly observes state changes with collectAsState()
- Error responses now show helpful demo messages instead of "Unknown error"
- Send icon updated to use AutoMirrored version

### Changed
- Removed complex dropdown model selection in favor of simple model indicator
- Updated API client to use network IP (26.202.89.251:11434) for phone testing
- Improved error messages to guide users through Ollama setup
- Enhanced ChatRepository with proper exception handling
- Default model changed to CodeLlama 13B Instruct for enhanced coding assistance

### Technical
- Migrated from basic state variables to StateFlow
- Added proper coroutine scope management with viewModelScope
- Implemented proper Android ViewModel lifecycle
- Updated Compose state management patterns
- Fixed deprecated icon usage

## [1.03] - 2025-09-20
- Previous version with initial improvements

## [1.0] - Initial Release
- Basic chat interface
- Ollama integration
- Room database setup
- Material Design 3 theming