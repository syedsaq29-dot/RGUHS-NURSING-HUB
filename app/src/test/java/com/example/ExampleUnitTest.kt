package com.example

import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testFetchDatabase() = runBlocking {
    try {
      println("--- STARTING FETCH TEST ---")
      val db = RetrofitClient.apiService.getDatabase("a20ffed648ea679c5ce2")
      println("--- FETCH SUCCESS ---")
      println("App Name: ${db.appConfig.appName}")
      println("Database Version: ${db.appConfig.dbVersion}")
      println("Latest APK Version: ${db.appConfig.latestApkVersion}")
      println("Latest APK URL: ${db.appConfig.apkDownloadUrl}")
    } catch (e: Exception) {
      println("--- FETCH FAILED ---")
      e.printStackTrace()
      fail(e.message)
    }
  }
}
