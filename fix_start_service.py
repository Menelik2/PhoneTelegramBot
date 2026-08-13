import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/BotService.kt', 'r') as f:
    content = f.read()

replacement = """        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, createNotification(), android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_CAMERA or android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE)
        } else {
            startForeground(NOTIFICATION_ID, createNotification())
        }"""

content = content.replace("startForeground(NOTIFICATION_ID, createNotification())", replacement)

with open('/app/applet/app/src/main/java/com/example/telegrambot/BotService.kt', 'w') as f:
    f.write(content)
