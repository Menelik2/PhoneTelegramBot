import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/MainActivity.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'Manifest.permission.CALL_PHONE',
    'Manifest.permission.CALL_PHONE,\n            Manifest.permission.RECORD_AUDIO'
)

with open('/app/applet/app/src/main/java/com/example/telegrambot/MainActivity.kt', 'w') as f:
    f.write(content)
