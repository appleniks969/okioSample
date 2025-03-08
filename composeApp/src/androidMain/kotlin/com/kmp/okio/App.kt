package com.kmp.okio

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import kotlinprojectokio.composeapp.generated.resources.Res
import kotlinprojectokio.composeapp.generated.resources.compose_multiplatform
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kmp.okio.utils.*
import okio.ByteString.Companion.encodeUtf8
import okio.Path.Companion.toPath
import kotlinx.coroutines.launch

@Composable
@Preview
fun App() {
    val scope = rememberCoroutineScope()
    val scaffoldState = rememberScaffoldState()
    
    var operationResults by remember { mutableStateOf<List<OperationResult>>(emptyList()) }
    
    MaterialTheme {
        Scaffold(
            scaffoldState = scaffoldState,
            topBar = {
                TopAppBar(
                    title = { Text("Okio Utilities Demo") }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painterResource(Res.drawable.compose_multiplatform),
                    null,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                
                // File Operation Buttons
                OperationButtonRow(
                    title = "Basic File Operations",
                    description = "Read, write, and delete files",
                    onRunTests = {
                        scope.launch {
                            val results = runBasicFileOperations()
                            operationResults = results + operationResults
                        }
                    }
                )
                
                OperationButtonRow(
                    title = "Directory Operations",
                    description = "Create directories and list files",
                    onRunTests = {
                        scope.launch {
                            val results = runDirectoryOperations()
                            operationResults = results + operationResults
                        }
                    }
                )
                
                OperationButtonRow(
                    title = "ZIP Operations",
                    description = "Compress and decompress files",
                    onRunTests = {
                        scope.launch {
                            val results = runZipOperations()
                            operationResults = results + operationResults
                        }
                    }
                )
                
                OperationButtonRow(
                    title = "Clear Results",
                    description = "Clear the operation results list",
                    buttonText = "Clear",
                    onRunTests = {
                        operationResults = emptyList()
                    }
                )
                
                // Results
                Text(
                    "Operation Results",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp)
                )
                
                operationResults.forEach { result ->
                    OperationResultCard(result)
                }
            }
        }
    }
}

@Composable
fun OperationButtonRow(
    title: String,
    description: String,
    buttonText: String = "Run",
    onRunTests: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = description,
                fontSize = 14.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Button(
                onClick = onRunTests,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(buttonText)
            }
        }
    }
}

// Basic file operations - Write, Read, Delete
private fun runBasicFileOperations(): List<OperationResult> {
    val results = mutableListOf<OperationResult>()
    
    try {
        // Create test directory
        val cacheDir = getCacheDirectory()
        val testDir = cacheDir / "okio-test"
        val createDirResult = createDirectories(testDir)
        if (createDirResult.isError) {
            results.add(
                OperationResult(
                    operation = "Create Test Directory",
                    details = "Failed to create directory: ${(createDirResult as FileResult.Error).exception.message}",
                    success = false
                )
            )
            return results
        }
        
        // 1. Write text file
        val textFile = testDir / "sample.txt"
        val textContent = "Hello from Okio Utilities!"
        val writeResult = writeToFile(textFile, textContent)
        
        results.add(
            OperationResult(
                operation = "Write Text File",
                details = "Wrote text to $textFile",
                success = writeResult.isSuccess
            )
        )
        
        // 2. Read text file
        val readResult = readFromFile(textFile)
        val readContent = readResult.getOrNull() ?: "Failed to read file"
        
        results.add(
            OperationResult(
                operation = "Read Text File",
                details = "Content: $readContent",
                success = readResult.isSuccess && readContent == textContent
            )
        )
        
        // 3. Write binary file
        val binaryFile = testDir / "binary.dat"
        val binaryData = "Binary data sample".encodeUtf8()
        val writeBinaryResult = writeToFile(binaryFile, binaryData)
        
        results.add(
            OperationResult(
                operation = "Write Binary File",
                details = "Wrote binary data to $binaryFile (${binaryData.size} bytes)",
                success = writeBinaryResult.isSuccess
            )
        )
        
        // 4. Read binary file
        val readBinaryResult = readBytesFromFile(binaryFile)
        val binarySuccess = readBinaryResult.isSuccess && 
                           readBinaryResult.getOrNull()?.utf8() == "Binary data sample"
        
        results.add(
            OperationResult(
                operation = "Read Binary File",
                details = "Read ${readBinaryResult.getOrNull()?.size ?: 0} bytes",
                success = binarySuccess
            )
        )
        
    } catch (e: Exception) {
        results.add(
            OperationResult(
                operation = "File Operations",
                details = "Error: ${e.message}",
                success = false
            )
        )
    }
    
    return results
}

// Directory operations - List, Copy, Delete
private fun runDirectoryOperations(): List<OperationResult> {
    val results = mutableListOf<OperationResult>()
    
    try {
        // Create test directory
        val cacheDir = getCacheDirectory()
        val testDir = cacheDir / "okio-test"
        val createDirResult = createDirectories(testDir)
        if (createDirResult.isError) {
            results.add(
                OperationResult(
                    operation = "Create Test Directory",
                    details = "Failed to create directory: ${(createDirResult as FileResult.Error).exception.message}",
                    success = false
                )
            )
            return results
        }
        
        // Create a sample file for operations
        val textFile = testDir / "sample.txt"
        val textContent = "This is a sample file for directory operations"
        writeToFile(textFile, textContent)
        
        // List files in directory
        val listResult = listDirectory(testDir)
        val files = listResult.getOrNull() ?: emptyList()
        
        results.add(
            OperationResult(
                operation = "List Directory",
                details = "Found ${files.size} files in $testDir",
                success = listResult.isSuccess
            )
        )
        
        // Copy file
        val copyFile = testDir / "sample-copy.txt"
        val copyResult = copyFile(textFile, copyFile)
        
        results.add(
            OperationResult(
                operation = "Copy File",
                details = "Copied $textFile to $copyFile",
                success = copyResult.isSuccess
            )
        )
        
        // Delete file
        val deleteResult = delete(textFile)
        val deleteSuccess = deleteResult.isSuccess && !fileExists(textFile)
        
        results.add(
            OperationResult(
                operation = "Delete File",
                details = if (deleteSuccess) "Successfully deleted $textFile" else "Failed to delete file",
                success = deleteSuccess
            )
        )
        
    } catch (e: Exception) {
        results.add(
            OperationResult(
                operation = "Directory Operations",
                details = "Error: ${e.message}",
                success = false
            )
        )
    }
    
    return results
}

// ZIP operations - Compress and decompress
private fun runZipOperations(): List<OperationResult> {
    val results = mutableListOf<OperationResult>()
    
    try {
        // Create test directory with content
        val cacheDir = getCacheDirectory()
        val contentDir = cacheDir / "zip-content"
        val createDirResult = createDirectories(contentDir)
        if (createDirResult.isError) {
            results.add(
                OperationResult(
                    operation = "Create Content Directory",
                    details = "Failed to create directory: ${(createDirResult as FileResult.Error).exception.message}",
                    success = false
                )
            )
            return results
        }
        
        // Create files in the content directory
        val contentFile = contentDir / "text-file.txt"
        val zipContent = "This content will be compressed into a ZIP file."
        val writeResult = writeToFile(contentFile, zipContent)
        if (writeResult.isError) {
            results.add(
                OperationResult(
                    operation = "Create Content File",
                    details = "Failed to create content file: ${(writeResult as FileResult.Error).exception.message}",
                    success = false
                )
            )
            return results
        }
        
        // Compress the directory
        val contentZip = cacheDir / "content.zip"
        val compressResult = compressToZip(contentDir, contentZip)
        
        results.add(
            OperationResult(
                operation = "Compress to ZIP",
                details = "Compressed $contentDir to $contentZip",
                success = compressResult.isSuccess && fileExists(contentZip)
            )
        )
        
        // Decompress the ZIP
        val extractDir = cacheDir / "extracted"
        val decompressResult = decompressZip(contentZip, extractDir)
        
        results.add(
            OperationResult(
                operation = "Decompress ZIP",
                details = "Extracted $contentZip to $extractDir",
                success = decompressResult.isSuccess && fileExists(extractDir)
            )
        )
        
        // Read directly from ZIP
        val readZipResult = readStringFromZip(contentZip, "text-file.txt")
        val zipReadSuccess = readZipResult.isSuccess && 
                           readZipResult.getOrNull() == zipContent
        
        results.add(
            OperationResult(
                operation = "Read from ZIP",
                details = "Content from ZIP: ${readZipResult.getOrNull() ?: "Failed to read"}",
                success = zipReadSuccess
            )
        )
        
        // Clean up
        delete(contentDir, recursively = true)
        delete(contentZip)
        delete(extractDir, recursively = true)
        
    } catch (e: Exception) {
        results.add(
            OperationResult(
                operation = "ZIP Operations",
                details = "Error: ${e.message}",
                success = false
            )
        )
    }
    
    return results
}

@Composable
fun OperationResultCard(result: OperationResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = if (result.success) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = if (result.success) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (result.success) "Success" else "Failure",
                tint = if (result.success) Color(0xFF4CAF50) else Color(0xFFF44336)
            )
            
            Column {
                Text(
                    text = result.operation,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.details,
                    fontSize = 14.sp
                )
            }
        }
    }
}

data class OperationResult(
    val operation: String,
    val details: String,
    val success: Boolean
)