package com.kmp.okio.utils

import okio.ByteString
import okio.FileSystem
import okio.Path
import okio.IOException
import kotlin.random.Random

// ==================== File System Operations ====================

/**
 * Represents the result of a file operation that might fail.
 */
sealed class FileResult<out T> {
    /**
     * Represents a successful file operation.
     */
    data class Success<T>(val value: T) : FileResult<T>()
    
    /**
     * Represents a failed file operation.
     */
    data class Error(val exception: Exception) : FileResult<Nothing>()
    
    /**
     * Returns the value if this is a [Success], or null if this is an [Error].
     */
    fun getOrNull(): T? = when (this) {
        is Success -> value
        is Error -> null
    }
    
    /**
     * Returns the value if this is a [Success], or throws the exception if this is an [Error].
     */
    fun getOrThrow(): T = when (this) {
        is Success -> value
        is Error -> throw exception
    }
    
    /**
     * Returns true if this is a [Success].
     */
    val isSuccess: Boolean get() = this is Success
    
    /**
     * Returns true if this is an [Error].
     */
    val isError: Boolean get() = this is Error
}

/**
 * Exception thrown when a file operation fails.
 */
class FileOperationException(message: String, cause: Throwable? = null) : IOException(message, cause)

/**
 * Writes the given [content] to a file at [path], creating parent directories if needed.
 *
 * @param path The path to the file.
 * @param content The string content to write.
 * @param createParentDirectories Whether to create parent directories if they don't exist.
 * @return A [FileResult] indicating success or failure.
 */
fun writeToFile(path: Path, content: String, createParentDirectories: Boolean = true): FileResult<Unit> {
    return try {
        if (createParentDirectories) {
            path.parent?.let { fileSystem.createDirectories(it) }
        }
        
        fileSystem.write(path) {
            writeUtf8(content)
        }
        FileResult.Success(Unit)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to write to file: $path", e))
    }
}

/**
 * Writes the given [byteString] to a file at [path], creating parent directories if needed.
 *
 * @param path The path to the file.
 * @param byteString The binary content to write.
 * @param createParentDirectories Whether to create parent directories if they don't exist.
 * @return A [FileResult] indicating success or failure.
 */
fun writeToFile(path: Path, byteString: ByteString, createParentDirectories: Boolean = true): FileResult<Unit> {
    return try {
        if (createParentDirectories) {
            path.parent?.let { fileSystem.createDirectories(it) }
        }
        
        fileSystem.write(path) {
            write(byteString)
        }
        FileResult.Success(Unit)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to write binary data to file: $path", e))
    }
}

/**
 * Reads the content from a file at [path] as a string.
 *
 * @param path The path to the file.
 * @return A [FileResult] containing the string content or an error.
 */
fun readFromFile(path: Path): FileResult<String> {
    return try {
        if (!fileSystem.exists(path)) {
            return FileResult.Error(FileOperationException("File does not exist: $path"))
        }
        
        val content = fileSystem.read(path) {
            readUtf8()
        }
        FileResult.Success(content)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to read from file: $path", e))
    }
}

/**
 * Reads the content from a file at [path] as a ByteString.
 *
 * @param path The path to the file.
 * @return A [FileResult] containing the binary content or an error.
 */
fun readBytesFromFile(path: Path): FileResult<ByteString> {
    return try {
        if (!fileSystem.exists(path)) {
            return FileResult.Error(FileOperationException("File does not exist: $path"))
        }
        
        val content = fileSystem.read(path) {
            readByteString()
        }
        FileResult.Success(content)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to read bytes from file: $path", e))
    }
}

/**
 * Checks if a file or directory exists at [path].
 *
 * @param path The path to check.
 * @return True if the path exists, false otherwise.
 */
fun fileExists(path: Path): Boolean {
    return fileSystem.exists(path)
}

/**
 * Deletes a file or directory at [path].
 * For directories, this only deletes empty directories unless [recursively] is true.
 *
 * @param path The path to the file or directory.
 * @param recursively Whether to delete directories recursively.
 * @return A [FileResult] indicating success or failure.
 */
fun delete(path: Path, recursively: Boolean = false): FileResult<Unit> {
    return try {
        if (!fileSystem.exists(path)) {
            return FileResult.Success(Unit) // Nothing to delete
        }
        
        if (recursively && fileSystem.metadata(path).isDirectory) {
            fileSystem.list(path).forEach { childPath ->
                val result = delete(childPath, true)
                if (result.isError) {
                    return result
                }
            }
        }
        
        fileSystem.delete(path)
        FileResult.Success(Unit)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to delete: $path", e))
    }
}

/**
 * Copies a file from [source] to [destination].
 *
 * @param source The path to the source file.
 * @param destination The path to the destination file.
 * @param createParentDirectories Whether to create parent directories for the destination.
 * @return A [FileResult] indicating success or failure.
 */
fun copyFile(source: Path, destination: Path, createParentDirectories: Boolean = true): FileResult<Unit> {
    return try {
        if (!fileSystem.exists(source)) {
            return FileResult.Error(FileOperationException("Source file doesn't exist: $source"))
        }
        
        if (fileSystem.metadata(source).isDirectory) {
            return FileResult.Error(FileOperationException("Source is a directory, not a file: $source"))
        }
        
        if (createParentDirectories) {
            destination.parent?.let { fileSystem.createDirectories(it) }
        }
        
        fileSystem.copy(source, destination)
        FileResult.Success(Unit)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to copy file from $source to $destination", e))
    }
}

/**
 * Creates all directories in the path if they don't exist.
 *
 * @param path The directory path to create.
 * @return A [FileResult] indicating success or failure.
 */
fun createDirectories(path: Path): FileResult<Unit> {
    return try {
        fileSystem.createDirectories(path)
        FileResult.Success(Unit)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to create directories: $path", e))
    }
}

/**
 * Lists all files and directories in the specified directory.
 *
 * @param directory The directory to list.
 * @return A [FileResult] containing a list of paths for all entries in the directory or an error.
 */
fun listDirectory(directory: Path): FileResult<List<Path>> {
    return try {
        if (!fileSystem.exists(directory)) {
            return FileResult.Error(FileOperationException("Directory doesn't exist: $directory"))
        }
        
        if (!fileSystem.metadata(directory).isDirectory) {
            return FileResult.Error(FileOperationException("Path is not a directory: $directory"))
        }
        
        FileResult.Success(fileSystem.list(directory))
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to list directory: $directory", e))
    }
}

/**
 * Extracts a ZIP file and reads the string content from a file within it.
 * 
 * @param zipPath The path to the ZIP file to extract.
 * @param filePathInZip The relative path to the file within the ZIP to read (optional).
 *                     If null, the first file in the extracted directory will be read.
 *                     If specified but not found, the function will try to search for the file by name only.
 * @param deleteAfterReading Whether to delete the extracted files after reading (default: true).
 * @return A [FileResult] containing the string content of the file from the ZIP or an error.
 */
fun readStringFromZip(zipPath: Path, filePathInZip: String? = null, deleteAfterReading: Boolean = true): FileResult<String> {
    if (!fileExists(zipPath)) {
        return FileResult.Error(FileOperationException("ZIP file does not exist: $zipPath"))
    }
    
    // Create a temporary directory for extraction with a random identifier
    val randomId = Random.nextInt(100000, 999999)
    val extractDir = getTempDirectory() / "extract_$randomId"
    
    try {
        // Create the temporary directory
        val createDirResult = createDirectories(extractDir)
        if (createDirResult.isError) {
            return FileResult.Error(FileOperationException("Failed to create temporary directory for ZIP extraction", (createDirResult as FileResult.Error).exception))
        }
        
        // Extract the ZIP file
        val extractResult = decompressZip(zipPath, extractDir)
        if (extractResult.isError) {
            return FileResult.Error(FileOperationException("Failed to extract ZIP file", (extractResult as FileResult.Error).exception))
        }
        
        // Find the file to read
        val fileToRead = if (filePathInZip != null) {
            // First try the exact specified path
            val exactPath = extractDir / filePathInZip
            if (fileExists(exactPath)) {
                exactPath
            } else {
                // If not found, try to find the file by name anywhere in the extracted directory
                val fileName = filePathInZip.substringAfterLast('/')
                val allFilesResult = findAllFiles(extractDir)
                
                if (allFilesResult.isError) {
                    return FileResult.Error(FileOperationException("Failed to search for file in extracted ZIP", (allFilesResult as FileResult.Error).exception))
                }
                
                val allFiles = allFilesResult.getOrThrow()
                val matchingFile = allFiles.find { it.name == fileName }
                
                if (matchingFile != null) {
                    matchingFile
                } else {
                    return FileResult.Error(FileOperationException("File '$filePathInZip' not found in the ZIP archive"))
                }
            }
        } else {
            // Find the first file in the extracted directory
            val allFilesResult = findAllFiles(extractDir)
            
            if (allFilesResult.isError) {
                return FileResult.Error(FileOperationException("Failed to list files in extracted ZIP", (allFilesResult as FileResult.Error).exception))
            }
            
            val allFiles = allFilesResult.getOrThrow()
            if (allFiles.isEmpty()) {
                return FileResult.Error(FileOperationException("No files found in the ZIP archive"))
            }
            
            allFiles.first()
        }
        
        // Read the file content
        return readFromFile(fileToRead)
    } catch (e: Exception) {
        return FileResult.Error(FileOperationException("Error processing ZIP file", e))
    } finally {
        // Clean up if requested
        if (deleteAfterReading) {
            delete(extractDir, recursively = true)
        }
    }
}

/**
 * Recursively finds all files (not directories) within a directory.
 * 
 * @param directory The directory to search.
 * @return A [FileResult] containing a list of paths to all files found or an error.
 */
private fun findAllFiles(directory: Path): FileResult<List<Path>> {
    if (!fileExists(directory) || !fileSystem.metadata(directory).isDirectory) {
        return FileResult.Success(emptyList())
    }
    
    return try {
        val directoryListResult = listDirectory(directory)
        if (directoryListResult.isError) {
            return directoryListResult
        }
        
        val result = mutableListOf<Path>()
        for (path in directoryListResult.getOrThrow()) {
            val metadata = fileSystem.metadata(path)
            if (metadata.isDirectory) {
                val subDirResult = findAllFiles(path)
                if (subDirResult.isError) {
                    return subDirResult
                }
                result.addAll(subDirResult.getOrThrow())
            } else {
                result.add(path)
            }
        }
        
        FileResult.Success(result)
    } catch (e: Exception) {
        FileResult.Error(FileOperationException("Failed to find files in directory: $directory", e))
    }
} 