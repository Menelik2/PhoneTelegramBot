package com.example.telegrambot.helpers

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.NetworkInterface
import java.net.URL
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object AdvancedControlHelper {

    private var isTorchOn = false
    private var tts: TextToSpeech? = null

    // ───────────────────────────── Torch / Flashlight ─────────────────────────────

    fun toggleTorch(context: Context): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "❌ No flashlight available on this device"

            isTorchOn = !isTorchOn
            cameraManager.setTorchMode(cameraId, isTorchOn)
            if (isTorchOn) "🔦 Flashlight turned *ON*" else "🔦 Flashlight turned *OFF*"
        } catch (e: Exception) {
            "❌ Failed to control flashlight: ${e.message}"
        }
    }

    fun setTorch(context: Context, on: Boolean): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                val chars = cameraManager.getCameraCharacteristics(id)
                chars.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: return "❌ No flashlight available on this device"

            isTorchOn = on
            cameraManager.setTorchMode(cameraId, on)
            if (on) "🔦 Flashlight turned *ON*" else "🔦 Flashlight turned *OFF*"
        } catch (e: Exception) {
            "❌ Failed to control flashlight: ${e.message}"
        }
    }

    // ───────────────────────────── Vibrate ─────────────────────────────

    fun vibrate(context: Context, durationMs: Long = 1000): String {
        return try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
            "📳 Device vibrated for ${durationMs}ms"
        } catch (e: Exception) {
            "❌ Failed to vibrate: ${e.message}"
        }
    }

    // ───────────────────────────── Volume ─────────────────────────────

    fun getVolumeInfo(context: Context): String {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val media = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxMedia = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val ring = am.getStreamVolume(AudioManager.STREAM_RING)
            val maxRing = am.getStreamMaxVolume(AudioManager.STREAM_RING)
            val alarm = am.getStreamVolume(AudioManager.STREAM_ALARM)
            val maxAlarm = am.getStreamMaxVolume(AudioManager.STREAM_ALARM)
            val notification = am.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            val maxNotif = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION)

            """
            🔊 *Volume Levels*
            
            • Media: $media / $maxMedia (${percent(media, maxMedia)}%)
            • Ring: $ring / $maxRing (${percent(ring, maxRing)}%)
            • Alarm: $alarm / $maxAlarm (${percent(alarm, maxAlarm)}%)
            • Notification: $notification / $maxNotif (${percent(notification, maxNotif)}%)
            • Ringer Mode: ${ringerModeName(am.ringerMode)}
            """.trimIndent()
        } catch (e: Exception) {
            "❌ Failed to get volume: ${e.message}"
        }
    }

    fun setMediaVolume(context: Context, percent: Int): String {
        return try {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val target = ((percent.coerceIn(0, 100) / 100.0) * max).toInt()
            am.setStreamVolume(AudioManager.STREAM_MUSIC, target, 0)
            "🔊 Media volume set to $percent% ($target / $max)"
        } catch (e: Exception) {
            "❌ Failed to set volume: ${e.message}"
        }
    }

    // ───────────────────────────── Toast on device ─────────────────────────────

    fun showToast(context: Context, message: String): String {
        return try {
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context.applicationContext, message, Toast.LENGTH_LONG).show()
            }
            "💬 Toast shown on device: \"$message\""
        } catch (e: Exception) {
            "❌ Failed to show toast: ${e.message}"
        }
    }

    // ───────────────────────────── Text-to-Speech ─────────────────────────────

    fun speak(context: Context, text: String): String {
        return try {
            val latch = CountDownLatch(1)
            var success = false

            android.os.Handler(android.os.Looper.getMainLooper()).post {
                tts = TextToSpeech(context.applicationContext) { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        tts?.language = Locale.getDefault()
                        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "bot_speak")
                        success = true
                    }
                    latch.countDown()
                }
            }

            latch.await(3, TimeUnit.SECONDS)
            if (success) "🗣️ Speaking: \"$text\"" else "❌ Text-to-speech not available"
        } catch (e: Exception) {
            "❌ Failed to speak: ${e.message}"
        }
    }

    // ───────────────────────────── Network / IP ─────────────────────────────

    fun getLocalIp(): String {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is java.net.Inet4Address) {
                        return addr.hostAddress ?: "Unknown"
                    }
                }
            }
            "Not found"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    suspend fun getPublicIp(): String = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = URL("https://api.ipify.org")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 5000
            conn.readTimeout = 5000
            conn.inputStream.bufferedReader().use { it.readText().trim() }
        } catch (e: Exception) {
            "Unavailable"
        }
    }

    fun getNetworkInfo(context: Context): String {
        return try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val network = cm.activeNetwork
            val caps = cm.getNetworkCapabilities(network)

            val type = when {
                caps == null -> "None"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile Data"
                caps.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
                else -> "Other"
            }

            "🌐 Network: *$type*\n📍 Local IP: `${getLocalIp()}`"
        } catch (e: Exception) {
            "🌐 Network: Unknown"
        }
    }

    // ───────────────────────────── Uptime ─────────────────────────────

    fun getUptime(): String {
        return try {
            val uptimeMs = android.os.SystemClock.elapsedRealtime()
            val seconds = uptimeMs / 1000
            val days = seconds / 86400
            val hours = (seconds % 86400) / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            "⏱️ Device uptime: ${days}d ${hours}h ${minutes}m ${secs}s"
        } catch (e: Exception) {
            "⏱️ Uptime: Unknown"
        }
    }

    // ───────────────────────────── Apps ─────────────────────────────

    fun getInstalledApps(context: Context, limit: Int = 40): String {
        return try {
            val pm = context.packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .sortedBy { it.loadLabel(pm).toString().lowercase() }

            if (packages.isEmpty()) return "📱 No user apps found"

            val sb = StringBuilder("📱 *Installed Apps* (${packages.size} user apps)\n\n")
            packages.take(limit).forEachIndexed { index, app ->
                val label = app.loadLabel(pm)
                sb.append("${index + 1}. $label\n   `${app.packageName}`\n")
            }
            if (packages.size > limit) {
                sb.append("\n_...and ${packages.size - limit} more_")
            }
            sb.toString()
        } catch (e: Exception) {
            "❌ Failed to list apps: ${e.message}"
        }
    }

    fun openApp(context: Context, packageOrName: String): String {
        return try {
            val pm = context.packageManager
            var intent = pm.getLaunchIntentForPackage(packageOrName)
            if (intent == null) {
                val match = pm.getInstalledApplications(0).firstOrNull {
                    it.loadLabel(pm).toString().equals(packageOrName, ignoreCase = true) ||
                            it.packageName.contains(packageOrName, ignoreCase = true)
                }
                if (match != null) {
                    intent = pm.getLaunchIntentForPackage(match.packageName)
                }
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                "🚀 Opening app: `$packageOrName`"
            } else {
                "❌ App not found: `$packageOrName`\nUse /apps to see available packages"
            }
        } catch (e: Exception) {
            "❌ Failed to open app: ${e.message}"
        }
    }

    // ───────────────────────────── Brightness ─────────────────────────────

    fun getBrightness(context: Context): String {
        return try {
            val brightness = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
            val percent = (brightness * 100 / 255)
            "☀️ Screen brightness: $percent% ($brightness/255)"
        } catch (e: Exception) {
            "☀️ Brightness: Unable to read (needs WRITE_SETTINGS permission)"
        }
    }

    // ───────────────────────────── Helpers ─────────────────────────────

    private fun percent(current: Int, max: Int): Int {
        return if (max == 0) 0 else (current * 100 / max)
    }

    private fun ringerModeName(mode: Int): String {
        return when (mode) {
            AudioManager.RINGER_MODE_NORMAL -> "Normal"
            AudioManager.RINGER_MODE_VIBRATE -> "Vibrate"
            AudioManager.RINGER_MODE_SILENT -> "Silent"
            else -> "Unknown"
        }
    }
}
