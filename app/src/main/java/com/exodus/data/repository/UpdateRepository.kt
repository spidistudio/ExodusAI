package com.exodus.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import com.exodus.data.model.GitHubRelease
import com.exodus.data.model.UpdateInfo
import com.exodus.utils.AppLogger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpdateRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val owner = "spidistudio"
    private val repo = "ExodusAI"
    
    suspend fun checkForUpdates(): UpdateInfo = withContext(Dispatchers.IO) {
        try {
            AppLogger.d("UpdateRepository", "Checking for updates...")
            
            val currentVersion = getCurrentAppVersion()
            AppLogger.d("UpdateRepository", "Current app version: $currentVersion")
            
            val apiUrl = "https://api.github.com/repos/$owner/$repo/releases/latest"
            val url = URL(apiUrl)
            val connection = url.openConnection() as HttpURLConnection
            
            try {
                connection.requestMethod = "GET"
                connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
                connection.connectTimeout = 10000
                connection.readTimeout = 10000
                
                val responseCode = connection.responseCode
                AppLogger.d("UpdateRepository", "API response code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.use { it.readText() }
                    AppLogger.d("UpdateRepository", "API response received")
                    
                    val jsonObject = JSONObject(response)
                    
                    if (!jsonObject.optBoolean("draft", false)) {
                        val tagName = jsonObject.getString("tag_name")
                        val name = jsonObject.getString("name")
                        val body = jsonObject.getString("body")
                        val htmlUrl = jsonObject.getString("html_url")
                        val publishedAt = jsonObject.getString("published_at")
                        val prerelease = jsonObject.optBoolean("prerelease", false)
                        
                        // Look for APK download URL in assets
                        var downloadUrl: String? = null
                        val assetsArray = jsonObject.optJSONArray("assets")
                        if (assetsArray != null) {
                            for (i in 0 until assetsArray.length()) {
                                val asset = assetsArray.getJSONObject(i)
                                val assetName = asset.getString("name")
                                if (assetName.endsWith(".apk")) {
                                    downloadUrl = asset.getString("browser_download_url")
                                    break
                                }
                            }
                        }
                        
                        val release = GitHubRelease(
                            tagName = tagName,
                            name = name,
                            body = body,
                            htmlUrl = htmlUrl,
                            downloadUrl = downloadUrl,
                            publishedAt = publishedAt,
                            prerelease = prerelease,
                            draft = false
                        )
                        
                        val latestVersion = parseVersionFromTag(release.tagName)
                        AppLogger.d("UpdateRepository", "Latest release version: $latestVersion")
                        
                        val isUpdateAvailable = isVersionNewer(latestVersion, currentVersion)
                        AppLogger.d("UpdateRepository", "Update available: $isUpdateAvailable")
                        
                        return@withContext UpdateInfo(
                            isUpdateAvailable = isUpdateAvailable,
                            latestVersion = latestVersion,
                            currentVersion = currentVersion,
                            releaseInfo = if (isUpdateAvailable) release else null
                        )
                    }
                }
                
                AppLogger.e("UpdateRepository", "Failed to fetch releases: $responseCode")
                return@withContext UpdateInfo(
                    isUpdateAvailable = false,
                    latestVersion = "",
                    currentVersion = currentVersion,
                    releaseInfo = null
                )
                
            } finally {
                connection.disconnect()
            }
            
        } catch (e: Exception) {
            AppLogger.e("UpdateRepository", "Error checking for updates", e)
            return@withContext UpdateInfo(
                isUpdateAvailable = false,
                latestVersion = "",
                currentVersion = getCurrentAppVersion(),
                releaseInfo = null
            )
        }
    }
    
    private fun getCurrentAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.23"
        } catch (e: PackageManager.NameNotFoundException) {
            AppLogger.e("UpdateRepository", "Failed to get app version", e)
            "1.23"
        }
    }
    
    private fun parseVersionFromTag(tagName: String): String {
        // Remove 'v' prefix if present (e.g., "v1.22" -> "1.22")
        return tagName.removePrefix("v")
    }
    
    private fun isVersionNewer(latestVersion: String, currentVersion: String): Boolean {
        try {
            val latestParts = latestVersion.split(".").map { it.toInt() }
            val currentParts = currentVersion.split(".").map { it.toInt() }
            
            // Compare major.minor versions
            for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
                val latest = latestParts.getOrNull(i) ?: 0
                val current = currentParts.getOrNull(i) ?: 0
                
                when {
                    latest > current -> return true
                    latest < current -> return false
                    // Continue if equal
                }
            }
            
            return false // Versions are equal
        } catch (e: Exception) {
            AppLogger.e("UpdateRepository", "Error comparing versions: $latestVersion vs $currentVersion", e)
            return false
        }
    }
    
    fun openUpdatePage(release: GitHubRelease) {
        try {
            // First try to open the direct download URL if available
            val downloadUrl = release.downloadUrl
            if (!downloadUrl.isNullOrEmpty()) {
                AppLogger.d("UpdateRepository", "Opening APK download: $downloadUrl")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } else {
                // Fallback to GitHub release page
                AppLogger.d("UpdateRepository", "Opening GitHub release page: ${release.htmlUrl}")
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(release.htmlUrl)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
        } catch (e: Exception) {
            AppLogger.e("UpdateRepository", "Error opening update page", e)
        }
    }
}