package com.max.notchflowmax.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
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
        const val VERSION_URL = "https://raw.githubusercontent.com/Mohamed-maxX/NotchFlow-Max/main/version.json"
    }

    private val _status = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val status: StateFlow<UpdateStatus> = _status.asStateFlow()

    suspend fun checkForUpdates(manual: Boolean = false) {
        _status.value = UpdateStatus.Checking
        withContext(Dispatchers.IO) {
            try {
                val url = URL(VERSION_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 10000
                    readTimeout = 10000
                    requestMethod = "GET"
                    setRequestProperty("User-Agent", "NotchFlowMax-Android")
                }

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(response)

                    val remoteVersionCode = json.getInt("versionCode")
                    val remoteVersionName = json.getString("versionName")
                    val apkUrl = json.getString("apkUrl")
                    val changelog = json.optString("changelog", "")

                    val currentVersionCode = BuildConfig.VERSION_CODE

                    if (remoteVersionCode > currentVersionCode) {
                        _status.value = UpdateStatus.UpdateAvailable(
                            UpdateInfo(
                                versionCode = remoteVersionCode,
                                versionName = remoteVersionName,
                                apkUrl = apkUrl,
                                changelog = changelog
                            )
                        )
                    } else {
                        _status.value = UpdateStatus.UpToDate
                    }
                } else {
                    _status.value = UpdateStatus.Error("Server response: ${connection.responseCode}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _status.value = UpdateStatus.Error(e.localizedMessage ?: "Failed to check update")
            }
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

                val url = URL(info.apkUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 15000
                    readTimeout = 30000
                    instanceFollowRedirects = true
                    setRequestProperty("User-Agent", "NotchFlowMax-Android")
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                    data = Uri.parse("package:${context.packageName}")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                return
            }
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
    }

    fun resetStatus() {
        _status.value = UpdateStatus.Idle
    }
}
