package com.kmp.okio.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import okio.ByteString.Companion.encodeUtf8
import okio.Buffer

/**
 * Tests for the serialization utilities in OkioUtils
 */
class OkioUtilsTest {
    @Test
    fun testSerializationUtils() {
        val testString = "Test String"
        val testByteString = "Binary Data".encodeUtf8()
        val testList = listOf("item1", "item2", "item3")
        val testMap = mapOf("key1" to "value1", "key2" to "value2")
        
        // Create a memory buffer for testing
        val buffer = Buffer()
        
        // Test writing serialized data
        buffer.writeLengthPrefixed(testByteString)
        buffer.writePrefixedString(testString)
        buffer.writeStringList(testList)
        buffer.writeStringMap(testMap)
        
        // Test reading serialized data
        val readByteString = buffer.readLengthPrefixed()
        val readString = buffer.readPrefixedString()
        val readList = buffer.readStringList()
        val readMap = buffer.readStringMap()
        
        // Verify the data
        assertEquals(testByteString, readByteString)
        assertEquals(testString, readString)
        assertEquals(testList, readList)
        assertEquals(testMap, readMap)
    }
} 