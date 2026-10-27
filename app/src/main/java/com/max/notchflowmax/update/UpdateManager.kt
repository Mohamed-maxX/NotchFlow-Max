package com.max.notchflowmax.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.FileProvider
import com.max.notchflowmax.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

class UpdateManager(private val context: Context) {

    companion object {
        const val RELEASES_API_URL = "https://api.github.com/repos/Mohamed-maxX/NotchFlow-Max/releases/latest"
    }

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    suspend fun checkForUpdates(isManualCheck: Boolean = false) {
        withContext(Dispatchers.IO) {
            try {
                val url = URL(RELEASES_API_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    useCaches = false
                    setRequestProperty("User-Agent", "NotchFlowMax-Android")
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("Cache-Control", "no-cache")
                    setRequestProperty("Pragma", "no-cache")
                }

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val tagName = json.getString("tag_name")
                    val cleanRemoteVersion = tagName.trim().removePrefix("v").removePrefix("V")
                    val currentVersion = BuildConfig.VERSION_NAME

                    // Extract body as changelog
                    val changelog = json.optString("body", "").trim()

                    // Extract direct APK browser_download_url from assets
                    var apkUrl: String? = null
                    val assets = json.optJSONArray("assets")
                    if (assets != null) {
                        for (i in 0 until assets.length()) {
                            val asset = assets.getJSONObject(i)
                            val name = asset.optString("name", "")
                            if (name.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.getString("browser_download_url")
                                break
                            }
                        }
                        if (apkUrl.isNullOrEmpty() && assets.length() > 0) {
                            apkUrl = assets.getJSONObject(0).optString("browser_download_url", "")
                        }
                    }

                    if (apkUrl.isNullOrEmpty()) {
                        apkUrl = "https://github.com/Mohamed-maxX/NotchFlow-Max/releases/latest/download/NotchFlowMax.apk"
                    }

                    if (isNewerVersion(cleanRemoteVersion, currentVersion)) {
                        _status.value = UpdateStatus.UpdateAvailable(
                            UpdateInfo(
                                versionName = cleanRemoteVersion,
                                apkUrl = apkUrl,
                                changelog = changelog
                            )
                        )
                    } else {
                        _status.value = UpdateStatus.Idle
                        if (isManualCheck) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "التطبيق محدث إلى آخر إصدار", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else if (responseCode == HttpURLConnection.HTTP_FORBIDDEN || responseCode == HttpURLConnection.HTTP_NOT_FOUND) {
                    _status.value = UpdateStatus.Idle
                    if (isManualCheck) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "تعذر الفحص حالياً، حاول لاحقاً", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    _status.value = UpdateStatus.Idle
                    if (isManualCheck) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "فشل الاتصال، تأكد من الإنترنت", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = UpdateStatus.Idle
                if (isManualCheck) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "فشل الاتصال، تأكد من الإنترنت", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        return try {
            val remoteParts = remoteVersion.trim().removePrefix("v").removePrefix("V").split(".").mapNotNull { it.toIntOrNull() }
            val currentParts = currentVersion.trim().removePrefix("v").removePrefix("V").split(".").mapNotNull { it.toIntOrNull() }

            val maxLen = maxOf(remoteParts.size, currentParts.size)
            for (i in 0 until maxLen) {
                val r = remoteParts.getOrElse(i) { 0 }
                val c = currentParts.getOrElse(i) { 0 }
                if (r > c) return true
                if (r < c) return false
            }
            false
        } catch (_: Exception) {
            false
        }
    }

    suspend fun downloadAndInstall(info: UpdateInfo) {
        _status.value = UpdateStatus.Downloading(0)
        withContext(Dispatchers.IO) {
            try {
                val destinationDir = File(context.cacheDir, "updates").apply { mkdirs() }
                val apkFile = File(destinationDir, "NotchFlowMax_${info.versionName}.apk")
                if (apkFile.exists()) {
                    apkFile.delete()
                }

                var currentUrl = info.apkUrl
                var connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "NotchFlowMax-Android")
                }

                var redirects = 0
                while (connection.responseCode in 300..399 && redirects < 5) {
                    val location = connection.getHeaderField("Location") ?: break
                    connection.disconnect()
                    currentUrl = location
                    connection = (URL(currentUrl).openConnection() as HttpURLConnection).apply {
                        connectTimeout = 15000
                        readTimeout = 30000
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "NotchFlowMax-Android")
                    }
                    redirects++
                }

                val fileLength = connection.contentLength
                val input = BufferedInputStream(connection.inputStream)
                val output = FileOutputStream(apkFile)

                val data = ByteArray(8192)
                var total: Long = 0
                var count: Int
                var lastReportedPercent = 0

                while (input.read(data).also { count = it } != -1) {
                    total += count
                    output.write(data, 0, count)
                    if (fileLength > 0) {
                        val percent = ((total * 100) / fileLength).toInt()
                        if (percent != lastReportedPercent) {
                            lastReportedPercent = percent
                            _status.value = UpdateStatus.Downloading(percent)
                        }
                    }
                }

                output.flush()
                output.close()
                input.close()
                connection.disconnect()

                _status.value = UpdateStatus.ReadyToInstall(apkFile)
                withContext(Dispatchers.Main) {
                    installApk(apkFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = UpdateStatus.Error("Download failed: ${e.localizedMessage}")
            }
        }
    }

    fun installApk(file: File) {
        if (!file.exists()) return

        try {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }

            val apkUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(installIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun resetStatus() {
        _status.value = UpdateStatus.Idle
    }
}
