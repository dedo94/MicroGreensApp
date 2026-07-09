package com.dedo94.microgreensapp

import android.app.Application
import com.dedo94.microgreensapp.core.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MicroGreensApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.ensureChannel(this)
    }
}
