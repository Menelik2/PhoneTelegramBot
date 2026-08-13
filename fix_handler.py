import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

# Fix the duplicate line and incorrect format
content = content.replace("• /status - Get device battery and storage status\n                    • /status - Get device battery and storage status\n                    /ping - Check if bot is alive", "• /status - Get device battery and storage status\n                    • /ping - Check if bot is alive")

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
