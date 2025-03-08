import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    kotlin("plugin.serialization") version "1.9.0" // Optional: for serialization support
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Core Okio dependency - latest stable version
            implementation("com.squareup.okio:okio:3.7.0")
        }
        
        commonTest.dependencies {
            implementation(kotlin("test"))
            implementation("com.squareup.okio:okio-fakefilesystem:3.7.0")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
            // Optional: add mockk for mocking if needed
            // implementation("io.mockk:mockk:1.13.9")
        }
        
        androidMain.dependencies {
            // Android-specific dependencies if needed
        }
        
        iosMain.dependencies {
            // iOS-specific dependencies if needed
        }
    }
}

android {
    namespace = "com.kmp.okio.shared"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
