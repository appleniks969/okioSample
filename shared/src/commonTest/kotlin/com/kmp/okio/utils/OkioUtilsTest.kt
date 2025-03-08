package com.kmp.okio.utils

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.buffer
import okio.fakefilesystem.FakeFileSystem

class OkioUtilsTest {
    
    private val fakeFileSystem = FakeFileSystem()
    private val testDir = "/test".toPath()
    
    @Test
    fun testFileOperations() {
        // Set up test environment
        fakeFileSystem.createDirectories(testDir)
        val filePath = testDir / "test.txt"
        
        // Test writeToFile
        val content = "Hello, World!"
        fakeFileSystem.write(filePath) {
            writeUtf8(content)
        }
        
        // Test fileExists
        assertTrue(fakeFileSystem.exists(filePath))
        
        // Test readFromFile
        val readContent = fakeFileSystem.read(filePath) {
            readUtf8()
        }
        assertEquals(content, readContent)
        
        // Test copyFile
        val destPath = testDir / "test_copy.txt"
        fakeFileSystem.copy(filePath, destPath)
        assertTrue(fakeFileSystem.exists(destPath))
        
        // Test listDirectory
        val files = fakeFileSystem.list(testDir)
        assertEquals(2, files.size)
        
        // Test delete
        fakeFileSystem.delete(filePath)
        assertFalse(fakeFileSystem.exists(filePath))
    }
    
    @Test
    fun testSerializationUtils() {
        val testString = "Test String"
        val testByteString = "Binary Data".encodeUtf8()
        val testList = listOf("item1", "item2", "item3")
        val testMap = mapOf("key1" to "value1", "key2" to "value2")
        
        // Create a memory buffer for testing
        val buffer = okio.Buffer()
        
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