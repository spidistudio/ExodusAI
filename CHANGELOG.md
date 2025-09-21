# Changelog

All notable changes to ExodusAI will be documented in this file.

## [1.26] - 2025-09-21

### Fixed
- 🔧 **Word Document Recognition** - Word documents (.docx/.doc) now properly detected as documents instead of images
- **MIME Type Detection** - Enhanced detection for Microsoft Office document formats
- **Attachment Processing** - Documents route to DocumentProcessor, images to ImageEncoder

### Enhanced
- **Document Format Support** - Comprehensive MIME type patterns for Office documents
- **User Guidance** - Improved instructions for document content sharing with better formatting
- **Debugging System** - Detailed logging for attachment type detection and processing
- **Error Handling** - Better user feedback for unsupported document types

### Technical
- **AttachmentType.fromMimeType()** - Enhanced with comprehensive Office document patterns
- **DocumentProcessor** - Improved Word document detection and guidance messages
- **Code Architecture** - Clean separation between image and document processing pipelines

## [1.25] - 2025-09-21

### Added
- 🖼️ **AI Vision Support** - Send images directly to vision-enabled AI models for analysis
- **ImageEncoder Utility** - Base64 encoding system for image attachments
- **Smart Image Resizing** - Automatic optimization (max 1024x1024) for efficient processing
- **Memory Management** - Proper bitmap cleanup and resource management
- **Enhanced API Integration** - Ollama vision API support with image data transmission

### Enhanced
- **File Attachment System** - Images now processed by AI models, not just displayed
- **ChatMessage Model** - Added `images` field for vision API compatibility
- **Error Handling** - Improved image processing error recovery
- **Performance** - Optimized image encoding and transmission

### Technical
- Updated ChatRepository to include Context for file operations
- Enhanced JSON serialization to include image data when present
- Added comprehensive logging for image processing pipeline
- Updated dependency injection for Context access

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