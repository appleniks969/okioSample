package com.kmp.okio.utils

import okio.FileSystem
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

/**
 * Tests for FileOperations.kt using a FakeFileSystem.
 */
class FileOperationsTest {
    // The fake file system to use for testing
    private lateinit var fakeFs: FakeFileSystem
    
    // A test file path
    private val testFilePath = "/test/file.txt".toPath()
    
    // A test directory path
    private val testDirPath = "/test/dir".toPath()
    
    /**
     * Setup before each test.
     */
    @BeforeTest
    fun setup() {
        // Create a new fake file system
        fakeFs = FakeFileSystem()
        
        // Override the global fileSystem with our fake one
        // We use reflection to modify the backing field of the fileSystem property
        val platformFileManagerField = PlatformFile::class.java.getDeclaredField("fileSystem")
        platformFileManagerField.isAccessible = true
        platformFileManagerField.set(null, fakeFs)
    }
    
    /**
     * Cleanup after each test.
     */
    @AfterTest
    fun cleanup() {
        // No need to reset the fileSystem property; it will be overridden in setup()
    }
    
    /**
     * Test writing to and reading from a file.
     */
    @Test
    fun testWriteAndReadFile() {
        // Given
        val content = "Hello, World!"
        
        // When
        val writeResult = writeToFile(testFilePath, content)
        
        // Then
        assertTrue(writeResult.isSuccess)
        assertTrue(fakeFs.exists(testFilePath))
        
        // When
        val readResult = readFromFile(testFilePath)
        
        // Then
        assertTrue(readResult.isSuccess)
        assertEquals(content, readResult.getOrThrow())
    }
    
    /**
     * Test file existence check.
     */
    @Test
    fun testFileExists() {
        // Given
        fakeFs.write(testFilePath) {
            writeUtf8("Test content")
        }
        
        // When/Then
        assertTrue(fileExists(testFilePath))
        assertFalse(fileExists("/non/existent/file".toPath()))
    }
    
    /**
     * Test deleting a file.
     */
    @Test
    fun testDeleteFile() {
        // Given
        fakeFs.write(testFilePath) {
            writeUtf8("Test content")
        }
        
        // When
        val deleteResult = delete(testFilePath)
        
        // Then
        assertTrue(deleteResult.isSuccess)
        assertFalse(fakeFs.exists(testFilePath))
    }
    
    /**
     * Test copying a file.
     */
    @Test
    fun testCopyFile() {
        // Given
        val content = "Test content"
        val destinationPath = "/test/copy.txt".toPath()
        
        fakeFs.write(testFilePath) {
            writeUtf8(content)
        }
        
        // When
        val copyResult = copyFile(testFilePath, destinationPath)
        
        // Then
        assertTrue(copyResult.isSuccess)
        assertTrue(fakeFs.exists(destinationPath))
        
        val destContent = fakeFs.read(destinationPath) {
            readUtf8()
        }
        assertEquals(content, destContent)
    }
    
    /**
     * Test creating and listing directories.
     */
    @Test
    fun testDirectoryOperations() {
        // Given
        val file1 = testDirPath / "file1.txt"
        val file2 = testDirPath / "file2.txt"
        
        // When
        val createResult = createDirectories(testDirPath)
        
        // Then
        assertTrue(createResult.isSuccess)
        assertTrue(fakeFs.metadata(testDirPath).isDirectory)
        
        // Given
        fakeFs.write(file1) { writeUtf8("Content 1") }
        fakeFs.write(file2) { writeUtf8("Content 2") }
        
        // When
        val listResult = listDirectory(testDirPath)
        
        // Then
        assertTrue(listResult.isSuccess)
        val files = listResult.getOrThrow()
        assertEquals(2, files.size)
        assertTrue(files.contains(file1))
        assertTrue(files.contains(file2))
    }
    
    /**
     * Test reading from a non-existent file returns an error.
     */
    @Test
    fun testReadNonExistentFile() {
        // When
        val readResult = readFromFile("/non/existent/file".toPath())
        
        // Then
        assertTrue(readResult.isError)
        val exception = (readResult as FileResult.Error).exception
        assertTrue(exception is FileOperationException)
    }
} 