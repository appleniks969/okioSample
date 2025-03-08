package com.kmp.okio.utils

import android.content.Context
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.sink
import okio.source
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import okio.buffer
import okio.use

/**
 * Application context to access Android-specific directories.
 * Must be initialized by the application before using file system functions.
 */
lateinit var appContext: Context

/**
 * Android implementation of PlatformFile interface.
 */
class AndroidPlatformFile : PlatformFile {
    /**
     * The system file system.
     */
    override val fileSystem: FileSystem = FileSystem.SYSTEM

    override fun getCacheDirectory(): Path {
        if (!::appContext.isInitialized) {
            throw FileOperationException("Android app context is not initialized. Call initialize() with your application context before using file operations.")
        }
        return appContext.cacheDir.absolutePath.toPath()
    }

    override fun getFilesDirectory(): Path {
        if (!::appContext.isInitialized) {
            throw FileOperationException("Android app context is not initialized. Call initialize() with your application context before using file operations.")
        }
        return appContext.filesDir.absolutePath.toPath()
    }

    override fun getTempDirectory(): Path {
        if (!::appContext.isInitialized) {
            throw FileOperationException("Android app context is not initialized. Call initialize() with your application context before using file operations.")
        }
        val tempPath = getCacheDirectory() / "temp"
        try {
            if (!fileSystem.exists(tempPath)) {
                fileSystem.createDirectories(tempPath)
            }
            return tempPath
        } catch (e: Exception) {
            throw FileOperationException("Failed to access or create temporary directory", e)
        }
    }

    override fun compressToZip(source: Path, zipPath: Path) {
        if (!fileExists(source)) {
            throw FileOperationException("Source path does not exist: $source")
        }
        
        try {
            // Create parent directories if needed
            zipPath.parent?.let { fileSystem.createDirectories(it) }
            
            ZipOutputStream(fileSystem.sink(zipPath).buffer().outputStream()).use { zipOut ->
                compressPathToZip(source, source, zipOut)
            }
        } catch (e: Exception) {
            throw FileOperationException("Failed to compress to ZIP: $source -> $zipPath", e)
        }
    }

    override fun decompressZip(zipPath: Path, destination: Path) {
        if (!fileExists(zipPath)) {
            throw FileOperationException("ZIP file does not exist: $zipPath")
        }
        
        try {
            // Create destination directory if it doesn't exist
            fileSystem.createDirectories(destination)
            
            ZipFile(zipPath.toString()).use { zipFile ->
                zipFile.entries().asSequence().forEach { entry ->
                    val entryPath = destination / entry.name
                    
                    if (entry.isDirectory) {
                        fileSystem.createDirectories(entryPath)
                    } else {
                        // Create parent directories for the file
                        entryPath.parent?.let { fileSystem.createDirectories(it) }
                        
                        zipFile.getInputStream(entry).use { input ->
                            fileSystem.sink(entryPath).buffer().use { sink ->
                                input.copyTo(sink.outputStream())
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            throw FileOperationException("Failed to decompress ZIP: $zipPath -> $destination", e)
        }
    }
    
    /**
     * Helper method to compress a path to a ZIP output stream.
     * 
     * @param path The path to compress
     * @param basePath The base path for calculating relative paths
     * @param zipOut The ZipOutputStream to write to
     * @throws FileOperationException If compression fails
     */
    private fun compressPathToZip(path: Path, basePath: Path, zipOut: ZipOutputStream) {
        try {
            val metadata = fileSystem.metadata(path)
            
            if (metadata.isDirectory) {
                // Process all files in the directory
                fileSystem.list(path).forEach { childPath ->
                    compressPathToZip(childPath, basePath, zipOut)
                }
            } else {
                // Add file to zip
                val relativePath = path.toString().removePrefix(basePath.parent.toString()).removePrefix("/")
                zipOut.putNextEntry(ZipEntry(relativePath))
                
                fileSystem.source(path).buffer().use { source ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (source.read(buffer).also { bytesRead = it } != -1) {
                        zipOut.write(buffer, 0, bytesRead)
                    }
                }
                
                zipOut.closeEntry()
            }
        } catch (e: Exception) {
            throw FileOperationException("Failed to compress file: $path", e)
        }
    }
}

/**
 * Initializes the Android file functionality with the application context.
 * This must be called from the application's onCreate() method.
 *
 * @param context The application context
 */
fun initialize(context: Context) {
    appContext = context.applicationContext
}

/**
 * Singleton instance of Android-specific platform file implementation.
 */
actual val PlatformFileManager: PlatformFile = AndroidPlatformFile() 