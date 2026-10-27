package com.max.notchflowmax.update

data class UpdateInfo(
    val versionName: String,
    val apkUrl: String,
    val changelog: String,
    val versionCode: Int = 0
)
