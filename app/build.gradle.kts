import java.net.URL
import java.net.HttpURLConnection
import java.io.PrintWriter
import java.io.OutputStreamWriter
import java.io.FileInputStream
import java.io.BufferedReader
import java.io.InputStreamReader

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
    applicationId = "com.aistudio.rguhsnursing.zpjwyq"
    minSdk = 23
    targetSdk = 36
    versionCode = 83
    versionName = "1.0.23"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
      enableV1Signing = true
      enableV2Signing = true
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("debugConfig")
    }
    debug {
      isDebuggable = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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
  lint {
    checkReleaseBuilds = false
    abortOnError = false
  }
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
  // implementation(libs.firebase.ai)
  // implementation(libs.play.services.ads)
  implementation("com.applovin:applovin-sdk:13.0.1")
  implementation(libs.unity.ads)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  // implementation(libs.logging.interceptor)
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

tasks.register("uploadApk") {
    doLast {
        val apkFile = file("build/outputs/apk/debug/app-debug.apk")
        if (!apkFile.exists()) {
            throw GradleException("APK does not exist! Run assembleDebug first.")
        }
        println("Uploading APK key files to catbox.moe: " + apkFile.absolutePath)
        try {
            val boundary = "====" + System.currentTimeMillis() + "===="
            val url = URL("https://catbox.moe/user/api.php")
            val conn = url.openConnection() as HttpURLConnection
            conn.doOutput = true
            conn.doInput = true
            conn.useCaches = false
            conn.requestMethod = "POST"
            conn.setRequestProperty("Connection", "Keep-Alive")
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")

            val outputStream = conn.outputStream
            val writer = PrintWriter(OutputStreamWriter(outputStream, "UTF-8"), true)

            // Parameter: reqtype = fileupload
            writer.append("--$boundary").append("\r\n")
            writer.append("Content-Disposition: form-data; name=\"reqtype\"").append("\r\n\r\n")
            writer.append("fileupload").append("\r\n")
            writer.flush()

            // Parameter: fileToUpload = physical APK bytes
            writer.append("--$boundary").append("\r\n")
            writer.append("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\"RGUHS_Nursing_App.apk\"").append("\r\n")
            writer.append("Content-Type: application/vnd.android.package-archive").append("\r\n\r\n")
            writer.flush()

            val fileInputStream = FileInputStream(apkFile)
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }
            outputStream.flush()
            fileInputStream.close()

            writer.append("\r\n")
            writer.append("--$boundary--").append("\r\n")
            writer.flush()
            writer.close()

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseReader = BufferedReader(InputStreamReader(conn.inputStream))
                val response = responseReader.use { it.readText() }.trim()
                println("SUCCESS: Upload complete! Link is:")
                println("APK_URL:" + response)
            } else {
                println("FAILED with HTTP response: " + responseCode)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}



