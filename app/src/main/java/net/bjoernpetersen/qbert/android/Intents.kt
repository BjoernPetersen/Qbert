package net.bjoernpetersen.qbert.android

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri


fun intent(configure: (Intent.() -> Unit)?): Intent {
    return Intent().apply { configure?.let { it(this) } }
}

fun intent(copied: Intent, configure: (Intent.() -> Unit)?): Intent {
    return Intent(copied).apply { configure?.let { it(this) } }
}

fun intent(action: String, configure: (Intent.() -> Unit)?): Intent {
    return Intent(action).apply { configure?.let { it(this) } }
}

fun intent(action: String, uri: Uri, configure: (Intent.() -> Unit)?): Intent {
    return Intent(action, uri).apply { configure?.let { it(this) } }
}

inline fun <reified T> Context.intent(): Intent {
    return Intent(this, T::class.java)
}

inline fun <reified T> Context.intent(configure: (Intent.() -> Unit)): Intent {
    return Intent(this, T::class.java).apply(configure)
}

inline fun <reified T> Context.intent(
    action: String,
    uri: Uri,
    configure: (Intent.() -> Unit)
): Intent {
    return Intent(action, uri, this, T::class.java).apply(configure)
}

inline fun <reified T> Context.intent(
    action: String,
    uri: Uri
): Intent {
    return Intent(action, uri, this, T::class.java)
}

fun Intent.pendingActivity(context: Context) =
    PendingIntent.getActivity(context, 0, this, 0)
