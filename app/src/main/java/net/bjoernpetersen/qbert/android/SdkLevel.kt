package net.bjoernpetersen.qbert.android

import android.os.Build

fun withMinSdk(level: Int, action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= level) action()
}
