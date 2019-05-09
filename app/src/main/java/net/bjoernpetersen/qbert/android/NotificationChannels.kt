package net.bjoernpetersen.qbert.android

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.content.getSystemService
import net.bjoernpetersen.qbert.R

const val ONGOING_NOTIFICATION_ID = 1
const val ONGOING_NOTIFICATION_CHANNEL_ID = "qbert_ongoing"

fun Application.registerOngoingNotificationChannel() = withMinSdk(Build.VERSION_CODES.O) {
    val channel = NotificationChannel(
        ONGOING_NOTIFICATION_CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_MIN
    ).apply {
        description = getString(R.string.notification_channel_description)
    }

    val manager: NotificationManager = getSystemService()!!
    manager.createNotificationChannel(channel)
}
