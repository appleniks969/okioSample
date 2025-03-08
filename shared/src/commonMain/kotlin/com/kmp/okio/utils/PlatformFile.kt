package com.kmp.okio.utils

import okio.FileSystem
import okio.Path

/**
 * Platform-specific file system operations interface.
 * This helps to ensure platform-specific implementations follow a consistent pattern.
 */
interface PlatformFile {
    /**
     * Provides the platform-specific FileSystem instance.
     */
    val fileSystem: FileSystem
    
    /**
     * Returns the platform-specific cache directory path.
     * This is typically used for temporary files that can be deleted by the system.
     *
     * @return The cache directory path.
     * @throws FileOperationException If the directory cannot be accessed.
     */
    fun getCacheDirectory(): Path
    
    /**
     * Returns the platform-specific files/documents directory path.
     * This is typically used for user files that should persist.
     *
     * @return The files directory path.
     * @throws FileOperationException If the directory cannot be accessed.
     */
    fun getFilesDirectory(): Path
    
    /**
     * Returns a platform-specific temporary directory path.
     * This is used for truly temporary files that will be deleted after use.
     *
     * @return The temporary directory path.
     * @throws FileOperationException If the directory cannot be accessed.
     */
    fun getTempDirectory(): Path
    
    /**
     * Compresses the file or directory at [source] into a zip file at [zipPath].
     *
     * @param source The path to the file or directory to compress.
     * @param zipPath The path where the zip file will be created.
     * @throws FileOperationException If compression fails.
     */
    fun compressToZip(source: Path, zipPath: Path)
    
    /**
     * Decompresses the zip file at [zipPath] to the directory at [destination].
     *
     * @param zipPath The path to the zip file to decompress.
     * @param destination The directory where the contents will be extracted.
     * @throws FileOperationException If decompression fails.
     */
    fun decompressZip(zipPath: Path, destination: Path)
}

/**
 * Singleton for accessing platform-specific file functionality.
 */
expect val PlatformFileManager: PlatformFile 