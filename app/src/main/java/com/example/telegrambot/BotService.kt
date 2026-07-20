package com.example.telegrambot

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.example.telegrambot.data.AppDatabase
import com.example.telegrambot.data.MessageEntity
import com.example.telegrambot.data.MessageType
import com.example.telegrambot.handlers.MessageHandler
import com.example.telegrambot.network.TelegramApi
import com.example.telegrambot.utils.Preferences
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

class BotService : Service() {

    companion object {
        private const val CHANNEL_ID = "telegram_bot_channel"
        private const val NOTIFICATION_ID = 1001
        private const val POLL_INTERVAL = 2000L
        private const val MAX_MESSAGES_PER_POLL = 100
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var lastUpdateId: Long = 0
    private var isPolling = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        lastUpdateId = Preferences.getLastUpdateId(this)
        startPolling()
        schedulePeriodicTasks()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Telegram Bot",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Telegram bot is running in background"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🤖 Telegram Bot")
            .setContentText("Bot is running and listening for messages")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun startPolling() {
        if (isPolling) return
        isPolling = true

        serviceScope.launch {
            while (isPolling) {
                try {
                    val token = Preferences.getBotToken(this@BotService)
                    if (token.isNullOrEmpty()) {
                        delay(5000)
                        continue
                    }

                    val updates = TelegramApi.getUpdates(
                        token = token,
                        offset = if (lastUpdateId == 0L) null else lastUpdateId + 1,
                        limit = MAX_MESSAGES_PER_POLL,
                        timeout = 30
                    )

                    if (updates.isNotEmpty()) {
                        processUpdates(updates)
                        lastUpdateId = updates.last().updateId
                        Preferences.setLastUpdateId(this@BotService, lastUpdateId)
                    }

                    delay(POLL_INTERVAL)
                } catch (e: Exception) {
                    delay(5000)
                }
            }
        }
    }

    private suspend fun processUpdates(updates: List<TelegramApi.Update>) {
        val db = AppDatabase.getInstance(this@BotService)
        
        for (update in updates) {
            try {
                val existing = db.messageDao().getByUpdateId(update.updateId)
                if (existing != null) continue

                val message = when {
                    update.message != null -> {
                        val msg = update.message
                        MessageEntity(
                            updateId = update.updateId,
                            type = if (msg.text?.startsWith("/") == true) 
                                MessageType.COMMAND else MessageType.TEXT,
                            from = msg.from?.username ?: msg.from?.first_name ?: "Unknown",
                            chatId = msg.chat.id.toString(),
                            content = msg.text ?: "",
                            timestamp = msg.date * 1000,
                            rawData = update.toString()
                        )
                    }
                    update.callback_query != null -> {
                        val cb = update.callback_query
                        MessageEntity(
                            updateId = update.updateId,
                            type = MessageType.CALLBACK,
                            from = cb.from.username ?: cb.from.first_name ?: "Unknown",
                            chatId = cb.message?.chat?.id.toString(),
                            content = cb.data ?: "",
                            timestamp = System.currentTimeMillis(),
                            rawData = update.toString()
                        )
                    }
                    update.inline_query != null -> {
                        val iq = update.inline_query
                        MessageEntity(
                            updateId = update.updateId,
                            type = MessageType.INLINE_QUERY,
                            from = iq.from.username ?: iq.from.first_name ?: "Unknown",
                            chatId = "inline",
                            content = iq.query,
                            timestamp = System.currentTimeMillis(),
                            rawData = update.toString()
                        )
                    }
                    else -> null
                }

                if (message != null) {
                    db.messageDao().insert(message)
                    MessageHandler.processMessage(this@BotService, message)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun schedulePeriodicTasks() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CleanupWorker>(
            1, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "cleanup_work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        isPolling = false
        serviceScope.cancel()
        Preferences.setBotRunning(this, false)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    class CleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
        override suspend fun doWork(): Result {
            return try {
                val db = AppDatabase.getInstance(applicationContext)
                val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                db.messageDao().deleteOlderThan(cutoffTime)
                Result.success()
            } catch (e: Exception) {
                Result.retry()
            }
        }
    }
}
