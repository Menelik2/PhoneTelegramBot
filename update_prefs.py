import re

with open('/app/applet/app/src/main/java/com/example/telegrambot/utils/Preferences.kt', 'r') as f:
    content = f.read()

content = content.replace(
    '    fun getBotToken(context: Context): String? {\n        return getPrefs(context).getString(KEY_BOT_TOKEN, null)\n    }',
    '    fun getBotToken(context: Context): String? {\n        val stored = getPrefs(context).getString(KEY_BOT_TOKEN, null)\n        if (!stored.isNullOrEmpty()) return stored\n        return "8737241034:AAGtIGWu1KXUKncSyW6x_TmrV2c32kWWKhI"\n    }'
)

with open('/app/applet/app/src/main/java/com/example/telegrambot/utils/Preferences.kt', 'w') as f:
    f.write(content)
