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
import java.io.IOException
import okio.buffer
import okio.use

/**
 * Application context to access Android-specific directories.
 * Must be initialized by the application before using file system functions.
 */
lateinit var appContext: Context

/**
 * The system file system.
 */
actual val fileSystem: FileSystem = FileSystem.SYSTEM

actual fun getCacheDirectory(): Path = appContext.cacheDir.absolutePath.toPath()

actual fun getFilesDirectory(): Path = appContext.filesDir.absolutePath.toPath()

actual fun getTempDirectory(): Path = getCacheDirectory() / "temp"

actual fun compressToZip(source: Path, zipPath: Path) {
    if (!fileExists(source)) {
        throw Exception("Source path does not exist: $source")
    }
    
    // Create parent directories if needed
    zipPath.parent?.let { fileSystem.createDirectories(it) }
    
    ZipOutputStream(fileSystem.sink(zipPath).buffer().outputStream()).use { zipOut ->
        compressPathToZip(source, source, zipOut)
    }
}

private fun compressPathToZip(path: Path, basePath: Path, zipOut: ZipOutputStream) {
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
}

actual fun decompressZip(zipPath: Path, destination: Path) {
    if (!fileExists(zipPath)) {
        throw Exception("Zip file does not exist: $zipPath")
    }
    
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
} 