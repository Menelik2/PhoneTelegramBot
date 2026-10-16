# PhoneTelegramBot

**Turn your Android phone into a powerful Telegram bot for remote device control.**

This Android application runs a Telegram bot directly on your phone. You can control and monitor the device by sending simple commands from any Telegram chat (or group).

> ⚠️ **Important Disclaimer**  
> This project gives significant remote access to the device (camera, microphone, location, contacts, call logs, screenshots, etc.).  
> Use it only on devices you own and with full awareness of the privacy and security implications.  
> The author is not responsible for any misuse.

---

## Features

- **Camera control** – Take photos from front or back camera
- **Screenshot** – Capture the current screen
- **Audio recording** – Record short audio clips
- **Location** – Get current GPS location
- **Contacts & Call logs** – Export contacts and recent call history
- **Gallery** – List recent photos
- **Clipboard** – Read current clipboard content
- **Device status** – Battery, storage, network info
- **Remote actions** – Ring the phone, call forwarding helpers, etc.
- **Runs in background** – Persistent foreground service + auto-start on boot
- **Telegram Mini App** – Companion web interface included in `/web`

---

## Available Commands

| Command              | Description                              |
|----------------------|------------------------------------------|
| `/start`             | Welcome message & overview               |
| `/help`              | List all available commands              |
| `/camera` or `/camera_back` | Take photo with back camera         |
| `/camera_front`      | Take photo with front camera             |
| `/screenshot`        | Capture current screen                   |
| `/record`            | Record ~40 seconds of audio              |
| `/location`          | Get current device location              |
| `/contacts`          | Export contacts list                     |
| `/call_log`          | Export recent call logs                  |
| `/gallery`           | Show recent gallery photos               |
| `/clipboard`         | Get current clipboard text               |
| `/status`            | Battery, storage & device status         |
| `/ring`              | Make the phone ring                      |
| `/forward`           | Call forwarding related helpers          |
| `/ping`              | Check if bot is alive                    |
| `/time`              | Get current device time                  |
| `/info`              | Device information                       |
| `/echo <text>`       | Echo back the message                    |

---

## Requirements

- Android Studio (or Android SDK + command-line tools)
- Android device or emulator running **Android 8.0 (API 26)+**
- A Telegram Bot Token (from [@BotFather](https://t.me/BotFather))
- Java 17+ (for building)

---

## Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/Menelik2/PhoneTelegramBot.git
cd PhoneTelegramBot
```

### 2. Create a Telegram Bot

1. Open Telegram and talk to [@BotFather](https://t.me/BotFather)
2. Send `/newbot` and follow the instructions
3. Copy the **Bot Token** (looks like `123456789:ABCdefGHIjklMNOpqrsTUVwxyz`)

### 3. Configure the app

1. Open the project in **Android Studio**
2. Build and install the app on your phone
3. Open the app → enter your **Bot Token** and (optionally) the allowed Chat ID
4. Grant all requested permissions
5. Start the bot

> The bot token is stored securely in app preferences. Never hardcode it in the source code.

### 4. Build a Release APK (optional)

```bash
# Using the included helper script
./build_apk.sh

# Or manually
./gradlew :app:assembleRelease
```

The APK will be located at:
```
app/build/outputs/apk/release/app-release.apk
```

---

## Permissions

The app requests the following permissions (all necessary for the features):

- Internet & Network state
- Camera & Microphone
- Location (fine/coarse)
- Read Contacts
- Read Call Log
- Read Media / External Storage
- Foreground Service
- Receive Boot Completed
- Post Notifications
- Wake Lock

On Android 13+ you will also need to grant notification permission.

---

## Web Companion (Telegram Mini App)

A modern Telegram Mini App is included in the `web/` folder.

You can serve it locally with:

```bash
python web_server.py
# → http://localhost:3000
```

Or deploy the `web/` folder to any static host (Vercel, Netlify, GitHub Pages, etc.).

---

## Project Structure

```
PhoneTelegramBot/
├── app/                          # Main Android application
│   └── src/main/java/com/example/telegrambot/
│       ├── MainActivity.kt       # UI & bot control
│       ├── BotService.kt         # Background polling service
│       ├── handlers/             # Message command handlers
│       ├── helpers/              # Feature helpers (camera, location, etc.)
│       ├── network/              # Telegram API client
│       └── ...
├── web/                          # Telegram Mini App (static)
├── web_server.py                 # Simple local web server
├── build_apk.sh                  # Helper script to build release APK
└── README.md
```

---

## Security Notes

- Only allow trusted Chat IDs if possible (the app supports restricting to a specific chat).
- Keep your Bot Token private.
- Be extremely careful when granting permissions — this app can access sensitive device data.
- Consider using a secondary/dedicated device for this kind of remote access tool.

---

## Contributing

Pull requests and improvements are welcome!

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Open a Pull Request

---

## License

This project is provided as-is for educational and personal use.

---

Made with ❤️ by [Menelik2](https://github.com/Menelik2)
