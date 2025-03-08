/**
 * OkioUtils: A Kotlin Multiplatform library for Android and iOS
 * 
 * This library provides cross-platform file operations, serialization utilities,
 * and zip functionality using Okio.
 */

package com.kmp.okio.utils

import okio.BufferedSink
import okio.BufferedSource
import okio.ByteString
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.EOFException
import okio.IOException

/**
 * Provides the platform-specific [FileSystem] instance.
 */
expect val fileSystem: FileSystem

/**
 * Returns the platform-specific cache directory path.
 */
expect fun getCacheDirectory(): Path

/**
 * Returns the platform-specific files/documents directory path.
 */
expect fun getFilesDirectory(): Path

/**
 * Returns a platform-specific temporary directory path.
 */
expect fun getTempDirectory(): Path

// ==================== Serialization Utilities ====================

/**
 * Writes a [ByteString] to the sink, prefixed with its length as a 32-bit integer.
 *
 * @param byteString The [ByteString] to write.
 * @throws IOException If writing to the sink fails.
 */
fun BufferedSink.writeLengthPrefixed(byteString: ByteString) {
    writeInt(byteString.size)
    write(byteString)
}

/**
 * Reads a [ByteString] from the source, prefixed with its length as a 32-bit integer.
 *
 * @return The read [ByteString].
 * @throws EOFException If the source is exhausted before reading the complete [ByteString].
 * @throws IOException If reading from the source fails.
 */
fun BufferedSource.readLengthPrefixed(): ByteString {
    val size = readInt()
    if (size < 0) throw Exception("Negative length prefix: $size")
    return readByteString(size.toLong())
}

/**
 * Writes a length-prefixed string to the sink.
 *
 * @param value The string to write.
 */
fun BufferedSink.writePrefixedString(value: String) {
    val encoded = ByteString.of(*value.encodeToByteArray())
    writeInt(encoded.size)
    write(encoded)
}

/**
 * Reads a string from the source, prefixed with its byte length and encoded in UTF-8.
 *
 * @return The read string.
 * @throws EOFException If the source is exhausted before reading the complete string.
 * @throws IOException If reading from the source fails.
 */
fun BufferedSource.readPrefixedString(): String {
    val size = readInt()
    if (size < 0) throw Exception("Negative length prefix: $size")
    return readByteString(size.toLong()).utf8()
}

/**
 * Writes a list of strings to the sink, prefixed with the list size.
 *
 * @param list The list of strings to write.
 * @throws IOException If writing to the sink fails.
 */
fun BufferedSink.writeStringList(list: List<String>) {
    writeInt(list.size)
    list.forEach { writePrefixedString(it) }
}

/**
 * Reads a list of strings from the source, prefixed with the list size.
 *
 * @return The read list of strings.
 * @throws EOFException If the source is exhausted before reading the complete list.
 * @throws IOException If reading from the source fails.
 */
fun BufferedSource.readStringList(): List<String> {
    val size = readInt()
    if (size < 0) throw Exception("Negative list size: $size")
    return List(size) { readPrefixedString() }
}

/**
 * Writes a map of strings to the sink, prefixed with the map size.
 *
 * @param map The map of strings to write.
 * @throws IOException If writing to the sink fails.
 */
fun BufferedSink.writeStringMap(map: Map<String, String>) {
    writeInt(map.size)
    map.forEach { (key, value) ->
        writePrefixedString(key)
        writePrefixedString(value)
    }
}

/**
 * Reads a map of strings from the source, prefixed with the map size.
 *
 * @return The read map of strings.
 * @throws EOFException If the source is exhausted before reading the complete map.
 * @throws IOException If reading from the source fails.
 */
fun BufferedSource.readStringMap(): Map<String, String> {
    val size = readInt()
    if (size < 0) throw Exception("Negative map size: $size")
    return buildMap(size) {
        repeat(size) {
            val key = readPrefixedString()
            val value = readPrefixedString()
            put(key, value)
        }
    }
} 