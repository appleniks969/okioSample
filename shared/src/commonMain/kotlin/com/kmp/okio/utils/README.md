# OkioUtils Structure and KMM Best Practices

This package contains utilities for using Okio in a Kotlin Multiplatform project. The code has been structured according to KMM best practices to ensure proper separation of concerns and platform-specific implementations.

## Architecture

### Interface-based Platform Implementation

We've adopted an interface-based approach for platform-specific functionality:

1. **PlatformFile Interface**: Defines the contract for platform-specific file operations
   - Provides consistent API across platforms
   - Makes platform-specific requirements explicit

2. **AndroidPlatformFile**: Implements the interface for Android
   - Uses Android Context for directory access
   - Implements compression using java.util.zip

3. **IosPlatformFile**: Implements the interface for iOS
   - Uses NSSearchPathForDirectoriesInDomains for directory access
   - Implements compression using a bridge to native iOS code

### Pure Kotlin Common Code

1. **FileOperations.kt**: Platform-agnostic file operations
   - Read, write, copy, list, and delete files
   - Works with the abstract FileSystem from Okio

2. **OkioUtils.kt**: Serialization utilities and platform-independent API
   - Extension functions for BufferedSink and BufferedSource
   - Delegates to platform-specific implementations where needed

## KMM Best Practices Applied

1. **Consistent Error Handling**
   - Transformed platform-specific exceptions to generic exceptions
   - Provides helpful context in error messages

2. **Dependency Injection**
   - Platform-specific implementations are provided via singletons
   - Makes testing easier with mock implementations

3. **Improved Testability**
   - FakeFileSystem for testing file operations
   - Mock implementations for platform-specific code

4. **Clear API Boundaries**
   - Separated serialization from file operations
   - Platform-specific code is well-encapsulated

5. **Documentation**
   - KDoc comments for all public APIs
   - Clear explanations of functionality and error conditions

6. **Simplified Usage**
   - Top-level functions for common operations
   - Type-safe Builder pattern via Kotlin extension functions 