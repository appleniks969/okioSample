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
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Okio File Operations",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                
                // Main Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                operationResults = emptyList()
                                val results = runFileOperationsDemo()
                                operationResults = results
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Run Demo")
                    }
                    
                    Button(
                        onClick = {
                            operationResults = emptyList()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(backgroundColor = Color.LightGray)
                    ) {
                        Text("Clear")
                    }
                }
                
                // Results
                if (operationResults.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth()
                        ) {
                            Text(
                                "Operation Results",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Divider()
                            
                            operationResults.forEach { result ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (result.success) Icons.Default.Check else Icons.Default.Close,
                                        contentDescription = if (result.success) "Success" else "Error",
                                        tint = if (result.success) Color.Green else Color.Red,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Column(
                                        modifier = Modifier
                                            .padding(start = 16.dp)
                                            .weight(1f)
                                    ) {
                                        Text(
                                            result.operation,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            result.details,
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                Divider()
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun runFileOperationsDemo(): List<OperationResult> {
    val results = mutableListOf<OperationResult>()
    
    try {
        // Set up test directory
        val testDir = getFilesDirectory() / "okio-demo"
        createDirectories(testDir)
        results.add(OperationResult(
            operation = "Create Directory", 
            details = "Created directory: $testDir",
            success = true
        ))
        
        // Write a text file
        val textFile = testDir / "sample.txt"
        val textContent = "Hello from Okio Utils!\nThis is a sample text file."
        writeToFile(textFile, textContent)
        results.add(OperationResult(
            operation = "Write Text File", 
            details = "Wrote ${textContent.length} characters to $textFile",
            success = true
        ))
        
        // Read the text file
        val readContent = readFromFile(textFile)
        val readSuccess = readContent == textContent
        results.add(OperationResult(
            operation = "Read Text File", 
            details = if (readSuccess) "Successfully read file content" else "Content mismatch",
            success = readSuccess
        ))
        
        // Write binary data
        val binaryFile = testDir / "binary.data"
        val binaryData = "Binary data with special chars: ÆØÅ".encodeUtf8()
        writeToFile(binaryFile, binaryData)
        results.add(OperationResult(
            operation = "Write Binary File", 
            details = "Wrote ${binaryData.size} bytes to $binaryFile",
            success = true
        ))
        
        // Read binary data
        val readBinary = readBytesFromFile(binaryFile)
        val binarySuccess = readBinary == binaryData
        results.add(OperationResult(
            operation = "Read Binary File", 
            details = if (binarySuccess) "Successfully read binary content" else "Content mismatch",
            success = binarySuccess
        ))
        
        // List directory
        val files = listDirectory(testDir)
        results.add(OperationResult(
            operation = "List Directory", 
            details = "Found ${files.size} files: ${files.joinToString(", ") { it.name }}",
            success = true
        ))
        
        // Copy file
        val copyFile = testDir / "sample-copy.txt"
        copyFile(textFile, copyFile)
        results.add(OperationResult(
            operation = "Copy File", 
            details = "Copied $textFile to $copyFile",
            success = true
            ))
        
        // Delete file
        delete(textFile)
        val deleteSuccess = !fileExists(textFile)
        results.add(OperationResult(
            operation = "Delete File", 
            details = if (deleteSuccess) "Successfully deleted $textFile" else "Failed to delete file",
            success = deleteSuccess
        ))
        
        // Create ZIP file
        val zipFile = getFilesDirectory() / "demo.zip"
        compressToZip(testDir, zipFile)
        results.add(OperationResult(
            operation = "Create ZIP", 
            details = "Created ZIP file at $zipFile",
            success = true
        ))
        
        // Extract ZIP file
        val extractDir = getFilesDirectory() / "extracted"
        createDirectories(extractDir)
        decompressZip(zipFile, extractDir)
        results.add(OperationResult(
            operation = "Extract ZIP", 
            details = "Extracted to $extractDir",
            success = true
        ))
        
    } catch (e: Exception) {
        results.add(OperationResult(
            operation = "Error", 
            details = "Exception: ${e.message}",
            success = false
        ))
    }
    
    return results
}

data class OperationResult(
    val operation: String,
    val details: String,
    val success: Boolean
)