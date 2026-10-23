package com.max.notchflowmax

import android.app.Application

class NotchFlowApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: NotchFlowApp
            private set
    }
}
