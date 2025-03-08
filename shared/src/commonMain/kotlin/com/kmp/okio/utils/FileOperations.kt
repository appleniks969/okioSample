package com.kmp.okio.utils

import okio.ByteString
import okio.FileSystem
import okio.Path

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

// ==================== ZIP Operations ====================

/**
 * Compresses the file or directory at [source] into a zip file at [zipPath].
 *
 * @param source The path to the file or directory to compress.
 * @param zipPath The path where the zip file will be created.
 */
expect fun compressToZip(source: Path, zipPath: Path)

/**
 * Decompresses the zip file at [zipPath] to the directory at [destination].
 *
 * @param zipPath The path to the zip file to decompress.
 * @param destination The directory where the contents will be extracted.
 */
expect fun decompressZip(zipPath: Path, destination: Path) 