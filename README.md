This is a Kotlin Multiplatform project targeting Android, iOS.

* `/composeApp` is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - `commonMain` is for code that's common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple's CoreCrypto for the iOS part of your Kotlin app,
    `iosMain` would be the right folder for such calls.

* `/iosApp` contains iOS applications. Even if you're sharing your UI with Compose Multiplatform, 
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* `/shared` is for the code that will be shared between all targets in the project.
  The most important subfolder is `commonMain`. If preferred, you can add code to the platform-specific folders here too.


Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…

Kotlin Multiplatform Project
==========================

# Okio Utils - Kotlin Multiplatform

A Kotlin Multiplatform (KMP) library providing cross-platform file operations, serialization utilities, and ZIP functionality using Square's [Okio](https://square.github.io/okio/) library.

## Features

- **File Operations**: Read, write, copy, delete files with a simple API
- **Directory Operations**: Create, list, and traverse directories
- **ZIP Functionality**: Compress and decompress files/directories
- **Serialization Utilities**: Easily serialize and deserialize data structures
- **Cross-Platform**: Runs on Android and iOS with the same API

## Project Structure

- **shared**: Contains the multiplatform library code
  - `FileOperations.kt`: File system operations (read, write, copy, etc.)
  - `OkioUtils.kt`: Serialization utilities
  - Platform-specific implementations for Android and iOS

- **composeApp**: Sample Android app demonstrating the library features

## Usage

### Basic File Operations

```kotlin
// Writing to a file
val filePath = getFilesDirectory() / "sample.txt"
writeToFile(filePath, "Hello, World!")

// Reading from a file
val content = readFromFile(filePath)

// Checking if a file exists
val exists = fileExists(filePath)

// Deleting a file
delete(filePath)
```

### ZIP Operations

```kotlin
// Compressing a directory
val sourceDir = getFilesDirectory() / "data"
val zipFile = getFilesDirectory() / "archive.zip"
compressToZip(sourceDir, zipFile)

// Extracting a ZIP file
val extractDir = getFilesDirectory() / "extracted"
decompressZip(zipFile, extractDir)
```

### Serialization Utilities

```kotlin
// Using a buffer
val buffer = Buffer()

// Writing data
buffer.writePrefixedString("Hello")
buffer.writeStringList(listOf("item1", "item2"))
buffer.writeStringMap(mapOf("key1" to "value1", "key2" to "value2"))

// Reading data
val string = buffer.readPrefixedString()
val list = buffer.readStringList()
val map = buffer.readStringMap()
```

## Getting Started

1. Clone the repository
2. Open the project in Android Studio or IntelliJ IDEA
3. Run the sample app to see the library in action

## Dependencies

- Okio: 3.5.0
- Kotlin Multiplatform

OkioUtils is a Kotlin Multiplatform library providing cross-platform file operations, serialization utilities, and zip functionality using the Okio library. This library works across Android and iOS platforms with a consistent API.

## Features

- Platform-specific directory access (cache, files, temporary)
- File operations (read/write, copy, delete, list, etc.)
- Serialization utilities for strings, lists, maps, and binary data
- ZIP compression and decompression

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
import com.kmp.okio.utils.appContext

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
    }
}
```

### iOS Integration

No additional setup is needed for iOS. The library uses NSSearchPathForDirectoriesInDomains and other platform APIs for directory access. ZIP functionality is implemented using SSZipArchive, which is integrated through Cocoapods.

## Usage Examples

### File Operations

```kotlin
import com.kmp.okio.utils.*
import okio.Path.Companion.toPath

// Writing to a file
val filePath = getFilesDirectory() / "myfile.txt"
writeToFile(filePath, "Hello, World!")

// Reading from a file
val content = readFromFile(filePath)

// Copying files
val destination = getCacheDirectory() / "myfile_copy.txt"
copyFile(filePath, destination)

// Listing directory contents
val files = listDirectory(getFilesDirectory())
files.forEach { println(it) }

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
    val str = source.readPrefixedString()
    val list = source.readStringList()
    val map = source.readStringMap()
    
    println("String: $str")
    println("List: $list")
    println("Map: $map")
}
```

### ZIP Operations

```kotlin
import com.kmp.okio.utils.*
import okio.Path.Companion.toPath

// Compressing files/directories
val sourceDir = getFilesDirectory() / "documents"
val zipFile = getCacheDirectory() / "documents.zip"
compressToZip(sourceDir, zipFile)

// Decompressing ZIP files
val extractPath = getFilesDirectory() / "extracted"
decompressZip(zipFile, extractPath)
```

## License

This library is licensed under the MIT License - see the LICENSE file for details.# okioSample
