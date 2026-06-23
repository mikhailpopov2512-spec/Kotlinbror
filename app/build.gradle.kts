import java.util.Base64

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.rosbrowser.vkyzpa"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    fun ensureDebugKeystoreExists() {
      val keystoreFile = file("${rootDir}/debug.keystore")
      if (!keystoreFile.exists() || keystoreFile.length() < 100) {
        println("--- AUTO-GENERATING DEBUG KEYSTORE FROM EMBEDDED BASE64 ---")
        val baseContent = "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"
        try {
          val decodedBytes = Base64.getDecoder().decode(baseContent)
          keystoreFile.writeBytes(decodedBytes)
          println("--- AUTO-GENERATING DEBUG KEYSTORE SUCCESSFUL! ---")
        } catch (e: Exception) {
          e.printStackTrace()
        }
      }
    }

    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "my-upload-key.jks"
      val keystoreFile = if (keystorePath.startsWith("/") || keystorePath.contains(":/") || keystorePath.contains(":\\")) {
        file(keystorePath)
      } else {
        file("${rootDir}/$keystorePath")
      }
      if (keystoreFile.exists()) {
        storeFile = keystoreFile
        storePassword = System.getenv("STORE_PASSWORD") ?: "android"
        keyAlias = System.getenv("KEY_ALIAS") ?: "upload"
        keyPassword = System.getenv("KEY_PASSWORD") ?: "android"
      } else {
        // Fallback to debug.keystore if release keystore does not exist
        ensureDebugKeystoreExists()
        val debugKeystoreFile = file("${rootDir}/debug.keystore")
        storeFile = debugKeystoreFile
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
    }
    create("debugConfig") {
      ensureDebugKeystoreExists()
      val debugKeystoreFile = file("${rootDir}/debug.keystore")
      storeFile = debugKeystoreFile
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  // implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
}
