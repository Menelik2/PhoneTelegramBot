import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

# Fix the duplicate line and incorrect format
content = content.replace("• /record - Record 40 seconds of audio\n                    • /record - Record 40 seconds of audio\n                    /ping - Check if bot is alive", "• /record - Record 40 seconds of audio\n                    • /ping - Check if bot is alive")

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
