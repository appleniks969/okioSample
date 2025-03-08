package com.kmp.okio.utils

import okio.ByteString
import okio.FileSystem
import okio.Path
import kotlin.random.Random

// ==================== File System Operations ====================

/**
 * Writes the given [content] to a file at [path], creating parent directories if needed.
 *
 * @param path The path to the file.
 * @param content The string content to write.
 * @param createParentDirectories Whether to create parent directories if they don't exist.
 */
fun writeToFile(path: Path, content: String, createParentDirectories: Boolean = true) {
    if (createParentDirectories) {
        path.parent?.let { fileSystem.createDirectories(it) }
    }
    
    fileSystem.write(path) {
        writeUtf8(content)
    }
}

/**
 * Writes the given [byteString] to a file at [path], creating parent directories if needed.
 *
 * @param path The path to the file.
 * @param byteString The binary content to write.
 * @param createParentDirectories Whether to create parent directories if they don't exist.
 */
fun writeToFile(path: Path, byteString: ByteString, createParentDirectories: Boolean = true) {
    if (createParentDirectories) {
        path.parent?.let { fileSystem.createDirectories(it) }
    }
    
    fileSystem.write(path) {
        write(byteString)
    }
}

/**
 * Reads the content from a file at [path] as a string.
 *
 * @param path The path to the file.
 * @return The string content of the file.
 * @throws Exception If the file does not exist.
 */
fun readFromFile(path: Path): String {
    if (!fileSystem.exists(path)) {
        throw Exception("File does not exist: $path")
    }
    
    return fileSystem.read(path) {
        readUtf8()
    }
}

/**
 * Reads the content from a file at [path] as a ByteString.
 *
 * @param path The path to the file.
 * @return The binary content of the file.
 * @throws Exception If the file does not exist.
 */
fun readBytesFromFile(path: Path): ByteString {
    if (!fileSystem.exists(path)) {
        throw Exception("File does not exist: $path")
    }
    
    return fileSystem.read(path) {
        readByteString()
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
 */
fun delete(path: Path, recursively: Boolean = false) {
    if (!fileSystem.exists(path)) {
        return // Nothing to delete
    }
    
    if (recursively && fileSystem.metadata(path).isDirectory) {
        fileSystem.list(path).forEach { childPath ->
            delete(childPath, true)
        }
    }
    
    fileSystem.delete(path)
}

/**
 * Copies a file from [source] to [destination].
 *
 * @param source The path to the source file.
 * @param destination The path to the destination file.
 * @param createParentDirectories Whether to create parent directories for the destination.
 * @throws Exception If the source file doesn't exist or is a directory.
 */
fun copyFile(source: Path, destination: Path, createParentDirectories: Boolean = true) {
    if (!fileSystem.exists(source)) {
        throw Exception("Source file doesn't exist: $source")
    }
    
    if (fileSystem.metadata(source).isDirectory) {
        throw Exception("Source is a directory, not a file: $source")
    }
    
    if (createParentDirectories) {
        destination.parent?.let { fileSystem.createDirectories(it) }
    }
    
    fileSystem.copy(source, destination)
}

/**
 * Creates all directories in the path if they don't exist.
 *
 * @param path The directory path to create.
 */
fun createDirectories(path: Path) {
    fileSystem.createDirectories(path)
}

/**
 * Lists all files and directories in the specified directory.
 *
 * @param directory The directory to list.
 * @return A list of paths for all entries in the directory.
 * @throws Exception If the directory doesn't exist or is not a directory.
 */
fun listDirectory(directory: Path): List<Path> {
    if (!fileSystem.exists(directory)) {
        throw Exception("Directory doesn't exist: $directory")
    }
    
    if (!fileSystem.metadata(directory).isDirectory) {
        throw Exception("Path is not a directory: $directory")
    }
    
    return fileSystem.list(directory)
}

/**
 * Extracts a ZIP file and reads the string content from a file within it.
 * 
 * @param zipPath The path to the ZIP file to extract.
 * @param filePathInZip The relative path to the file within the ZIP to read (optional).
 *                     If null, the first file in the extracted directory will be read.
 *                     If specified but not found, the function will try to search for the file by name only.
 * @param deleteAfterReading Whether to delete the extracted files after reading (default: true).
 * @return The string content of the file from the ZIP.
 * @throws Exception If the ZIP file doesn't exist, extraction fails, or the specified file cannot be found.
 */
fun readStringFromZip(zipPath: Path, filePathInZip: String? = null, deleteAfterReading: Boolean = true): String {
    if (!fileExists(zipPath)) {
        throw Exception("ZIP file does not exist: $zipPath")
    }
    
    // Create a temporary directory for extraction with a random identifier
    val randomId = Random.nextInt(100000, 999999)
    val extractDir = getTempDirectory() / "extract_$randomId"
    try {
        // Create the temporary directory
        createDirectories(extractDir)
        
        // Extract the ZIP file
        decompressZip(zipPath, extractDir)
        
        // Find the file to read
        val fileToRead = if (filePathInZip != null) {
            // First try the exact specified path
            val exactPath = extractDir / filePathInZip
            if (fileExists(exactPath)) {
                exactPath
            } else {
                // If not found, try to find the file by name anywhere in the extracted directory
                val fileName = filePathInZip.substringAfterLast('/')
                val allFiles = findAllFiles(extractDir)
                val matchingFile = allFiles.find { it.name == fileName }
                
                if (matchingFile != null) {
                    matchingFile
                } else {
                    throw Exception("File '$filePathInZip' not found in the ZIP archive")
                }
            }
        } else {
            // Find the first file in the extracted directory
            val allFiles = findAllFiles(extractDir)
            if (allFiles.isEmpty()) {
                throw Exception("No files found in the ZIP archive")
            }
            allFiles.first()
        }
        
        // Read the file content
        return readFromFile(fileToRead)
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
 * @return A list of paths to all files found.
 */
private fun findAllFiles(directory: Path): List<Path> {
    if (!fileExists(directory) || !fileSystem.metadata(directory).isDirectory) {
        return emptyList()
    }
    
    val result = mutableListOf<Path>()
    for (path in listDirectory(directory)) {
        val metadata = fileSystem.metadata(path)
        if (metadata.isDirectory) {
            result.addAll(findAllFiles(path))
        } else {
            result.add(path)
        }
    }
    
    return result
} 