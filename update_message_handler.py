import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

# Add to welcome message
content = content.replace(
    "• /ping - Check if bot is alive",
    "• /record - Record 40 seconds of audio\n                    • /ping - Check if bot is alive"
)

# Add to help message
content = content.replace(
    "/ping - Check if bot is alive",
    "/record - Record 40 seconds of audio\n                    /ping - Check if bot is alive"
)

# Add handler
new_handler = """            "/record" -> {
                TelegramApi.sendMessage(token, chatId, "🎙️ Recording 40 seconds of audio...")
                com.example.telegrambot.helpers.AudioRecordHelper.recordAudio(context, 40000) { file ->
                    if (file != null) {
                        handlerScope.launch { TelegramApi.sendDocument(token, chatId, file) }
                    } else {
                        handlerScope.launch { TelegramApi.sendMessage(token, chatId, "❌ Failed to record audio. Check permissions.") }
                    }
                }
            }
            "/ping" -> {"""
            
content = content.replace('            "/ping" -> {', new_handler)

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
