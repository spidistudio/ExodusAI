# Changelog

All notable changes to ExodusAI will be documented in this file.

## [1.05] - 2025-09-20

### Added
- Full integration with user's custom CodeLlama model (codellama-custom:latest)
- Real-time AI responses through localhost Ollama connection
- Working chat functionality with 3.6GB CodeLlama Custom 13B model
- Verified API connectivity and response handling

### Fixed
- Updated API endpoints to use localhost (127.0.0.1:11434) for local testing
- Configured app to use actual available CodeLlama models
- Tested and verified full chat pipeline from app to Ollama server

### Changed
- Default model changed to "codellama-custom:latest" (user's specific model)
- UI updated to show "CodeLlama Custom 13B - Your personal coding assistant"
- Fallback models updated to match user's available models

### Technical
- Tested successful connection to running Ollama server
- Verified 7369 character AI response capability
- Confirmed model availability and chat API functionality

## [1.04] - 2025-09-20
- CodeLlama 13B Integration setup

## [1.03] - 2025-09-20
- Previous version with initial improvements

## [1.0] - Initial Release
- Basic chat interface
- Ollama integration
- Room database setup
- Material Design 3 theming