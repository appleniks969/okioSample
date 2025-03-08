# OkioUtils - Kotlin Multiplatform

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android%20|%20iOS-lightgrey.svg)](https://kotlinlang.org/docs/multiplatform.html)

A Kotlin Multiplatform (KMP) library providing cross-platform file operations, serialization utilities, and ZIP functionality using Square's [Okio](https://square.github.io/okio/) library.

## Features

- **File Operations**: Read, write, copy, delete files with a simple API
- **Directory Operations**: Create, list, and traverse directories
- **ZIP Functionality**: Compress and decompress files/directories
- **Serialization Utilities**: Easily serialize and deserialize data structures
- **Cross-Platform**: Runs on Android and iOS with the same API
- **Error Handling**: Modern result-based API that doesn't throw exceptions

## Project Structure

- **FileOperations.kt**: Common file system operations (read, write, copy, etc.)
- **OkioUtils.kt**: Serialization utilities and ZIP operations
- **PlatformFile.kt**: Cross-platform interface for platform-specific operations
- Platform-specific implementations for Android and iOS

## Setup

### Gradle

Add the dependency to your build.gradle.kts file:

```kotlin
dependencies {
    implementation("com.kmp.okio:shared:1.0.0")
}
```

### Android Setup

Initialize the app context in your Application class:

```kotlin
import com.kmp.okio.utils.initialize

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initialize(this) // Initialize with application context
    }
}
```

### iOS Integration

No additional setup is needed for iOS. The library automatically uses platform APIs for directory access. ZIP functionality is implemented using SSZipArchive.

## Usage Examples

### Basic File Operations with Result Handling

```kotlin
import com.kmp.okio.utils.*
import okio.Path.Companion.toPath

// Writing to a file
val filePath = getFilesDirectory() / "myfile.txt"
val writeResult = writeToFile(filePath, "Hello, World!")

if (writeResult.isSuccess) {
    println("File written successfully")
} else {
    println("Failed to write file: ${(writeResult as FileResult.Error).exception.message}")
}

// Reading from a file - using getOrNull() for nullable result
val content = readFromFile(filePath).getOrNull()
if (content != null) {
    println("Content: $content")
} else {
    println("Failed to read file")
}

// Reading from a file - using getOrThrow() to handle errors with try/catch
try {
    val content = readFromFile(filePath).getOrThrow()
    println("Content: $content")
} catch (e: Exception) {
    println("Failed to read file: ${e.message}")
}

// You can also use Kotlin's when expression for clear control flow
when (val result = readFromFile(filePath)) {
    is FileResult.Success -> println("Content: ${result.value}")
    is FileResult.Error -> println("Error: ${result.exception.message}")
}

// Copying files
val destination = getCacheDirectory() / "myfile_copy.txt"
copyFile(filePath, destination)

// Listing directory contents
val filesResult = listDirectory(getFilesDirectory())
filesResult.getOrNull()?.forEach { println(it) }

// Deleting files
delete(filePath)
```

### Serialization Utilities

```kotlin
import com.kmp.okio.utils.*
import okio.buffer
import okio.sink
import okio.source

// Example with BufferedSink/BufferedSource
val filePath = getFilesDirectory() / "data.bin"

// Writing data
fileSystem.sink(filePath).buffer().use { sink ->
    sink.writePrefixedString("Hello")
    sink.writeStringList(listOf("item1", "item2", "item3"))
    sink.writeStringMap(mapOf("key1" to "value1", "key2" to "value2"))
}

// Reading data
fileSystem.source(filePath).buffer().use { source ->
    try {
        val str = source.readPrefixedString()
        val list = source.readStringList()
        val map = source.readStringMap()
        
        println("String: $str")
        println("List: $list")
        println("Map: $map")
    } catch (e: SerializationException) {
        println("Failed to deserialize: ${e.message}")
    }
}
```

### ZIP Operations

```kotlin
import com.kmp.okio.utils.*
import okio.Path.Companion.toPath

// Compressing files/directories
val sourceDir = getFilesDirectory() / "documents"
val zipFile = getCacheDirectory() / "documents.zip"

val compressResult = compressToZip(sourceDir, zipFile)
if (compressResult.isSuccess) {
    println("Compression successful")
} else {
    println("Compression failed: ${(compressResult as FileResult.Error).exception.message}")
}

// Decompressing ZIP files
val extractPath = getFilesDirectory() / "extracted"
val extractResult = decompressZip(zipFile, extractPath)

if (extractResult.isSuccess) {
    println("Extraction successful")
} else {
    println("Extraction failed: ${(extractResult as FileResult.Error).exception.message}")
}

// Reading content directly from a ZIP file
val contentResult = readStringFromZip(zipFile, "document.txt")
val content = contentResult.getOrNull() ?: "Failed to read from ZIP"
println(content)
```

## Error Handling

This library uses a Result-based approach to handle errors instead of throwing exceptions. All operations that can fail return a `FileResult<T>` which is either a `Success` with a value or an `Error` with an exception.

```kotlin
sealed class FileResult<out T> {
    data class Success<T>(val value: T) : FileResult<T>()
    data class Error(val exception: Exception) : FileResult<Nothing>()
    
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Error -> null
    }
    
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Error -> throw exception
    }
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
}
```

This allows for more explicit error handling without try/catch blocks, making your code cleaner and more predictable.

## Platform-Specific Behavior

- **Android**: Uses Context to access app-specific directories
- **iOS**: Uses NSSearchPathForDirectoriesInDomains and NSTemporaryDirectory for directory access

## License

This library is licensed under the MIT License - see the LICENSE file for details.

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