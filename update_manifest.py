import re

with open('/app/applet/app/src/main/AndroidManifest.xml', 'r') as f:
    content = f.read()

content = content.replace(
    '<uses-permission android:name="android.permission.CAMERA" />',
    '<uses-permission android:name="android.permission.CAMERA" />\n    <uses-permission android:name="android.permission.RECORD_AUDIO" />\n    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MICROPHONE" />'
)

content = content.replace(
    'android:foregroundServiceType="dataSync|camera"',
    'android:foregroundServiceType="dataSync|camera|microphone"'
)

with open('/app/applet/app/src/main/AndroidManifest.xml', 'w') as f:
    f.write(content)
