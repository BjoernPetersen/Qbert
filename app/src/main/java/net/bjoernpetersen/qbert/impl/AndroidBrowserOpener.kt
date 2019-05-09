package net.bjoernpetersen.qbert.impl

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import mu.KotlinLogging
import net.bjoernpetersen.musicbot.spi.util.BrowserOpener
import java.net.URL

private class AndroidBrowserOpener(private val context: Context) : BrowserOpener {
    private val logger = KotlinLogging.logger {}
    override fun openDocument(url: URL) {
        logger.debug { "Opening URL $url" }
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        context.startActivity(intent)
    }
}

fun Application.createBrowserOpener(): BrowserOpener {
    return AndroidBrowserOpener(this)
}

fun Activity.createBrowserOpener(): BrowserOpener {
    return AndroidBrowserOpener(applicationContext)
}
