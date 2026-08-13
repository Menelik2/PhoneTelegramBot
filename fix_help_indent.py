import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'r') as f:
    content = f.read()

content = content.replace('            "/help" -> {', '            "/help" -> {').replace('                        "/help" -> {', '            "/help" -> {')

with open('/app/applet/app/src/main/java/com/example/telegrambot/handlers/MessageHandler.kt', 'w') as f:
    f.write(content)
