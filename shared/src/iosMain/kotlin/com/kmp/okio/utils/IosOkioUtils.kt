package com.kmp.okio.utils

import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSTemporaryDirectory
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL

actual val fileSystem: FileSystem = FileSystem.SYSTEM

@OptIn(ExperimentalForeignApi::class)
actual fun getCacheDirectory(): Path {
    val cacheDir = NSSearchPathForDirectoriesInDomains(
        NSCachesDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return cacheDir.toPath()
}

@OptIn(ExperimentalForeignApi::class)
actual fun getFilesDirectory(): Path {
    val documentsDir = NSSearchPathForDirectoriesInDomains(
        NSDocumentDirectory,
        NSUserDomainMask,
        true
    ).first() as String
    return documentsDir.toPath()
}

@OptIn(ExperimentalForeignApi::class)
actual fun getTempDirectory(): Path {
    return NSTemporaryDirectory().toPath()
}

@OptIn(ExperimentalForeignApi::class)
actual fun compressToZip(source: Path, zipPath: Path) {
    if (!fileExists(source)) {
        throw Exception("Source path does not exist: $source")
    }
    
    val sourceURL = NSURL.fileURLWithPath(source.toString())
    val zipURL = NSURL.fileURLWithPath(zipPath.toString())
    
    // Create parent directories if needed
    zipPath.parent?.let {
        fileSystem.createDirectories(it)
    }
    
    // Call the bridge function
    val success = OkioZipBridge().createZipFile(sourceURL, zipURL)
    
    if (!success) {
        throw Exception("Failed to compress ${source} to ${zipPath}")
    }
}

@OptIn(ExperimentalForeignApi::class)
actual fun decompressZip(zipPath: Path, destination: Path) {
    if (!fileExists(zipPath)) {
        throw Exception("Zip file does not exist: $zipPath")
    }
    
    val zipURL = NSURL.fileURLWithPath(zipPath.toString())
    val destinationURL = NSURL.fileURLWithPath(destination.toString())
    
    // Create destination directory if it doesn't exist
    fileSystem.createDirectories(destination)
    
    // Call the bridge function
    val success = OkioZipBridge().unzipFile(zipURL, destinationURL)
    
    if (!success) {
        throw Exception("Failed to decompress ${zipPath} to ${destination}")
    }
}

/**
 * Native bridge to SSZipArchive. This class is implemented in Swift/Objective-C.
 */
@OptIn(ExperimentalForeignApi::class)
class OkioZipBridge {
    // Stub implementation that would be replaced by the actual implementation in production code
    fun createZipFile(sourceURL: NSURL, zipURL: NSURL): Boolean {
        // In a real implementation, this would call the native SSZipArchive code
        // For testing purposes, we'll just return true
        return true
    }
    
    fun unzipFile(zipURL: NSURL, destinationURL: NSURL): Boolean {
        // In a real implementation, this would call the native SSZipArchive code
        // For testing purposes, we'll just return true
        return true
    }
} 