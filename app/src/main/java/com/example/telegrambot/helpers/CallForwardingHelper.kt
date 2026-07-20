package com.example.telegrambot.helpers

import android.content.Context
import android.content.Intent
import android.net.Uri

object CallForwardingHelper {
    fun setupForwarding(context: Context, number: String): Boolean {
        return try {
            val encodedHash = Uri.encode("#")
            val ussd = "*21*$number$encodedHash"
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ussd")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun disableForwarding(context: Context): Boolean {
        return try {
            val encodedHash = Uri.encode("#")
            val ussd = "##21$encodedHash"
            val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$ussd")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
