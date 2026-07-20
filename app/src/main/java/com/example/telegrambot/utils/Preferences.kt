package com.example.telegrambot.utils

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object Preferences {
    private const val PREFS_NAME = "telegram_bot_prefs"
    private const val KEY_BOT_TOKEN = "bot_token"
    private const val KEY_WEBHOOK_URL = "webhook_url"
    private const val KEY_BOT_RUNNING = "bot_running"
    private const val KEY_LAST_UPDATE_ID = "last_update_id"
    private const val KEY_SERVICE_START_TIME = "service_start_time"

    private fun getPrefs(context: Context): android.content.SharedPreferences {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            return EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    fun saveBotToken(context: Context, token: String) {
        getPrefs(context).edit().putString(KEY_BOT_TOKEN, token).apply()
    }

    fun getBotToken(context: Context): String? {
        return getPrefs(context).getString(KEY_BOT_TOKEN, null)
    }

    fun saveWebhookUrl(context: Context, url: String) {
        getPrefs(context).edit().putString(KEY_WEBHOOK_URL, url).apply()
    }

    fun getWebhookUrl(context: Context): String? {
        return getPrefs(context).getString(KEY_WEBHOOK_URL, null)
    }

    fun setBotRunning(context: Context, running: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BOT_RUNNING, running).apply()
    }

    fun isBotRunning(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BOT_RUNNING, false)
    }

    fun getLastUpdateId(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_UPDATE_ID, 0)
    }

    fun setLastUpdateId(context: Context, updateId: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_UPDATE_ID, updateId).apply()
    }

    fun isBotConfigured(context: Context): Boolean {
        return !getBotToken(context).isNullOrEmpty()
    }

    fun setServiceStartTime(context: Context, time: Long) {
        getPrefs(context).edit().putLong(KEY_SERVICE_START_TIME, time).apply()
    }

    fun getServiceStartTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_SERVICE_START_TIME, 0L)
    }
}
