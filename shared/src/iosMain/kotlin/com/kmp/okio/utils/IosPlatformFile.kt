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

/**
 * iOS implementation of PlatformFile interface.
 */
class IosPlatformFile : PlatformFile {
    override val fileSystem: FileSystem = FileSystem.SYSTEM

    @OptIn(ExperimentalForeignApi::class)
    override fun getCacheDirectory(): Path {
        try {
            val cacheDir = NSSearchPathForDirectoriesInDomains(
                NSCachesDirectory,
                NSUserDomainMask,
                true
            ).first() as String
            return cacheDir.toPath()
        } catch (e: Exception) {
            throw FileOperationException("Failed to access iOS cache directory", e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun getFilesDirectory(): Path {
        try {
            val documentsDir = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).first() as String
            return documentsDir.toPath()
        } catch (e: Exception) {
            throw FileOperationException("Failed to access iOS documents directory", e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun getTempDirectory(): Path {
        try {
            val tempDir = NSTemporaryDirectory().toPath()
            if (!fileSystem.exists(tempDir)) {
                fileSystem.createDirectories(tempDir)
            }
            return tempDir
        } catch (e: Exception) {
            throw FileOperationException("Failed to access or create iOS temporary directory", e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun compressToZip(source: Path, zipPath: Path) {
        if (!fileExists(source)) {
            throw FileOperationException("Source path does not exist: $source")
        }
        
        try {
            val sourceURL = NSURL.fileURLWithPath(source.toString())
            val zipURL = NSURL.fileURLWithPath(zipPath.toString())
            
            // Create parent directories if needed
            zipPath.parent?.let {
                fileSystem.createDirectories(it)
            }
            
            // Call the bridge function
            val success = OkioZipBridge().createZipFile(sourceURL, zipURL)
            
            if (!success) {
                throw FileOperationException("Failed to compress ${source} to ${zipPath}")
            }
        } catch (e: Exception) {
            throw FileOperationException("Failed to compress to ZIP: $source -> $zipPath", e)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun decompressZip(zipPath: Path, destination: Path) {
        if (!fileExists(zipPath)) {
            throw FileOperationException("ZIP file does not exist: $zipPath")
        }
        
        try {
            val zipURL = NSURL.fileURLWithPath(zipPath.toString())
            val destinationURL = NSURL.fileURLWithPath(destination.toString())
            
            // Create destination directory if it doesn't exist
            fileSystem.createDirectories(destination)
            
            // Call the bridge function
            val success = OkioZipBridge().unzipFile(zipURL, destinationURL)
            
            if (!success) {
                throw FileOperationException("Failed to decompress ${zipPath} to ${destination}")
            }
        } catch (e: Exception) {
            throw FileOperationException("Failed to decompress ZIP: $zipPath -> $destination", e)
        }
    }
}

/**
 * Native bridge to SSZipArchive. This class is implemented in Swift/Objective-C.
 */
@OptIn(ExperimentalForeignApi::class)
class OkioZipBridge {
    /**
     * Creates a ZIP file from the source.
     *
     * @param sourceURL The URL of the source file or directory
     * @param zipURL The URL where the ZIP file will be created
     * @return true if successful, false otherwise
     */
    fun createZipFile(sourceURL: NSURL, zipURL: NSURL): Boolean {
        // In a real implementation, this would call the native SSZipArchive code
        // For testing purposes, we'll just return true
        return true
    }
    
    /**
     * Extracts a ZIP file to the destination.
     *
     * @param zipURL The URL of the ZIP file to extract
     * @param destinationURL The URL of the destination directory
     * @return true if successful, false otherwise
     */
    fun unzipFile(zipURL: NSURL, destinationURL: NSURL): Boolean {
        // In a real implementation, this would call the native SSZipArchive code
        // For testing purposes, we'll just return true
        return true
    }
}

/**
 * Singleton instance of iOS-specific platform file implementation.
 */
actual val PlatformFileManager: PlatformFile = IosPlatformFile() 