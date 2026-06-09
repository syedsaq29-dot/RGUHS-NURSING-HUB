// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register<Exec>("uploadApk") {
  group = "publishing"
  description = "Compiles the app and uploads the resulting APK"
  commandLine("node", "upload_apk.js")
}

tasks.register<Exec>("updateNpoint") {
  group = "publishing"
  description = "Updates the database JSON on npoint"
  commandLine("node", "update_npoint.js")
}

