package net.bjoernpetersen.qbert

import android.app.Activity
import android.app.Application
import android.app.Service
import net.bjoernpetersen.qbert.android.registerOngoingNotificationChannel
import net.bjoernpetersen.qbert.impl.setupSqlite

class QbertApp : Application() {
    var isServiceRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()
        setupSqlite()
        registerOngoingNotificationChannel()
    }
}

val Activity.app: QbertApp
    get() = application as QbertApp
val Service.app: QbertApp
    get() = application as QbertApp
