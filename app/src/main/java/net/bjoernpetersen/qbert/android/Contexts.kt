package net.bjoernpetersen.qbert.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle

inline fun <reified T> Context.startActivity(options: Bundle? = null) {
    startActivity(Intent(this, T::class.java), options)
}
