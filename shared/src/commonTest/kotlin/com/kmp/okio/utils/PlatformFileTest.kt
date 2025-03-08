package com.kmp.okio.utils

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import okio.fakefilesystem.FakeFileSystem
import okio.FileSystem
import okio.Path

/**
 * Mock implementation of PlatformFile for testing
 */
class TestPlatformFile(private val fakeFileSystem: FakeFileSystem) : PlatformFile {
    override val fileSystem: FileSystem = fakeFileSystem
    
    private val testBaseDir = "/test".toPath()
    private val testCacheDir = testBaseDir / "cache"
    private val testFilesDir = testBaseDir / "files" 
    private val testTempDir = testBaseDir / "temp"
    
    init {
        fakeFileSystem.createDirectories(testCacheDir)
        fakeFileSystem.createDirectories(testFilesDir)
        fakeFileSystem.createDirectories(testTempDir)
    }
    
    override fun getCacheDirectory(): Path = testCacheDir
    
    override fun getFilesDirectory(): Path = testFilesDir
    
    override fun getTempDirectory(): Path = testTempDir
    
    override fun compressToZip(source: Path, zipPath: Path) {
        if (!fileSystem.exists(source)) {
            throw Exception("Source path does not exist: $source")
        }
        
        // Simulate zip creation by creating an empty zip file
        zipPath.parent?.let { fileSystem.createDirectories(it) }
        fileSystem.write(zipPath) {
            // Just write some dummy content to simulate a ZIP file
            write("ZIP".encodeUtf8())
        }
    }
    
    override fun decompressZip(zipPath: Path, destination: Path) {
        if (!fileSystem.exists(zipPath)) {
            throw Exception("Zip file does not exist: $zipPath")
        }
        
        // Simulate extraction by creating the directory
        fileSystem.createDirectories(destination)
        
        // Create a sample file to simulate extraction
        val extractedFile = destination / "extracted.txt"
        fileSystem.write(extractedFile) {
            writeUtf8("Extracted content")
        }
    }
}

// Global variable to hold the mock platform file manager during tests
private var mockPlatformFileManager: PlatformFile? = null

// Override the real PlatformFileManager with our test implementation
internal val TestPlatformFileManager: PlatformFile
    get() = mockPlatformFileManager ?: throw IllegalStateException("MockPlatformFileManager not initialized")

class PlatformFileTest {
    private lateinit var fakeFileSystem: FakeFileSystem
    private lateinit var testPlatformFile: TestPlatformFile
    
    @BeforeTest
    fun setup() {
        fakeFileSystem = FakeFileSystem()
        testPlatformFile = TestPlatformFile(fakeFileSystem)
        
        // Set the mock for tests
        mockPlatformFileManager = testPlatformFile
    }
    
    @Test
    fun testDirectories() {
        val cacheDir = TestPlatformFileManager.getCacheDirectory()
        val filesDir = TestPlatformFileManager.getFilesDirectory()
        val tempDir = TestPlatformFileManager.getTempDirectory()
        
        assertTrue(cacheDir.toString().contains("cache"))
        assertTrue(filesDir.toString().contains("files"))
        assertTrue(tempDir.toString().contains("temp"))
    }
    
    @Test
    fun testFileOperations() {
        // Get standard directories
        val filesDir = TestPlatformFileManager.getFilesDirectory()
        
        // Test file operations
        val filePath = filesDir / "test.txt"
        val content = "Hello, Kotlin Multiplatform!"
        
        // Write to file
        fakeFileSystem.write(filePath) {
            writeUtf8(content)
        }
        assertTrue(fakeFileSystem.exists(filePath))
        
        // Read from file
        val readContent = fakeFileSystem.read(filePath) {
            readUtf8()
        }
        assertEquals(content, readContent)
        
        // Binary operations
        val binaryPath = filesDir / "binary.dat"
        val binaryData = "Binary test: ÆØÅ".encodeUtf8()
        fakeFileSystem.write(binaryPath) {
            write(binaryData)
        }
        assertTrue(fakeFileSystem.exists(binaryPath))
        
        val readBinary = fakeFileSystem.read(binaryPath) {
            readByteString()
        }
        assertEquals(binaryData, readBinary)
        
        // Copy operation
        val copyPath = filesDir / "test_copy.txt"
        fakeFileSystem.copy(filePath, copyPath)
        assertTrue(fakeFileSystem.exists(copyPath))
        assertEquals(content, fakeFileSystem.read(copyPath) { readUtf8() })
        
        // List directory
        val files = fakeFileSystem.list(filesDir)
        // We've created test.txt and binary.dat and test_copy.txt, so there should be 3 files
        assertEquals(3, files.size)
        
        // Delete file
        fakeFileSystem.delete(filePath)
        assertFalse(fakeFileSystem.exists(filePath))
    }
    
    @Test
    fun testZipOperations() {
        val filesDir = TestPlatformFileManager.getFilesDirectory()
        val sourceDir = filesDir / "source"
        fakeFileSystem.createDirectories(sourceDir)
        
        // Create some test files
        val testFile = sourceDir / "test.txt"
        fakeFileSystem.write(testFile) {
            writeUtf8("Test content")
        }
        
        // Test compression
        val zipPath = filesDir / "archive.zip"
        TestPlatformFileManager.compressToZip(sourceDir, zipPath)
        assertTrue(fakeFileSystem.exists(zipPath))
        
        // Test extraction
        val extractDir = filesDir / "extracted"
        TestPlatformFileManager.decompressZip(zipPath, extractDir)
        assertTrue(fakeFileSystem.exists(extractDir))
        assertTrue(fakeFileSystem.exists(extractDir / "extracted.txt"))
    }
} 