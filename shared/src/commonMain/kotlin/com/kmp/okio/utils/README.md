# OkioUtils

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20|%20iOS-lightgrey.svg)](https://kotlinlang.org/docs/multiplatform.html)

A Kotlin Multiplatform library providing cross-platform file operations, serialization utilities, and ZIP functionality using Square's [Okio](https://square.github.io/okio/) library.

## Features

- **Cross-Platform File Operations**: Read, write, copy, delete, and list files with a consistent API
- **Platform-Specific Directory Access**: Get cache, files, and temp directories on iOS and Android
- **Serialization Utilities**: Easily serialize strings, lists, maps, and binary data
- **ZIP Functionality**: Compress and decompress files with the same API on all platforms
- **Modern Architecture**: Interface-based design with clean separation of platform-specific code

## Installation

### Gradle

Add the dependency to your module's `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
}

// For common code
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("com.kmp.okio:okio-utils:1.0.0")
            }
        }
    }
}

// For JVM/Android only
dependencies {
    implementation("com.kmp.okio:okio-utils-android:1.0.0")
}
```

### Cocoapods

Add the pod to your `Podfile`:

```ruby
pod 'OkioUtils', '~> 1.0.0'
```

## Usage

### File Operations

```kotlin
val file = FileSystem.SYSTEM.getPath("/path/to/file")

// Read file
val content = file.readText()

// Write file
file.writeText("Hello, World!")

// Copy file
val destination = FileSystem.SYSTEM.getPath("/path/to/destination")
file.copyTo(destination)

// Delete file
file.delete()

// List files in a directory
val directory = FileSystem.SYSTEM.getPath("/path/to/directory")
val files = directory.listFiles()
```

### Serialization Utilities

```kotlin
val data = mapOf("key" to "value")

// Serialize to JSON
val json = data.toJson()

// Deserialize from JSON
val deserializedData = json.fromJson<Map<String, String>>()
```

### ZIP Functionality

```kotlin
val source = FileSystem.SYSTEM.getPath("/path/to/source")
val destination = FileSystem.SYSTEM.getPath("/path/to/destination")

// Compress file or directory
source.compress(destination)

// Decompress file
destination.decompress(source)
```

## API Documentation

For detailed API documentation, please refer to the [KDoc](https://kotlinlang.org/docs/kotlin-doc.html) comments in the source code.

## Platform-Specific Details

- **Android**: Uses Android Context for directory access and Java's `java.util.zip` for compression
- **iOS**: Uses NSSearchPathForDirectoriesInDomains for directory access and a bridge to native iOS code for compression

## Contributing

Contributions are welcome! Please follow the [Contributing Guidelines](CONTRIBUTING.md) for more information.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

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
   - Includes ZIP extraction and reading utilities

2. **OkioUtils.kt**: Serialization utilities and platform-independent API
   - Extension functions for BufferedSink and BufferedSource
   - Delegates to platform-specific implementations where needed

## Key Features

### File Operations
```kotlin
// Basic operations
writeToFile(path, "Hello World")
val content = readFromFile(path)
delete(path)

// Directory operations
createDirectories(path)
val files = listDirectory(path)

// Copying files
copyFile(source, destination)
```

### ZIP Operations
```kotlin
// Compress files or directories
compressToZip(source, destination)

// Extract ZIP files
decompressZip(zipFile, extractDir)

// Extract and read content directly from ZIP
val content = readStringFromZip(zipFile, "path/to/file/in/zip.txt")
```

### Serialization
```kotlin
// Write data
buffer.writePrefixedString("Hello")
buffer.writeStringList(listOf("a", "b", "c"))
buffer.writeStringMap(mapOf("key" to "value"))

// Read data
val string = buffer.readPrefixedString()
val list = buffer.readStringList()
val map = buffer.readStringMap()
```

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