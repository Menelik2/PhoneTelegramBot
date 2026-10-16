package com.example.telegrambot.helpers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.text.DecimalFormat

object DeviceStatusHelper {
    fun getStatusMessage(context: Context): String {
        return buildString {
            appendLine("📱 *Device Status*")
            appendLine()
            appendLine("🔋 *Battery*: ${getBatteryStatus(context)}")
            appendLine("💾 *Storage*: ${getStorageStatus()}")
            appendLine(AdvancedControlHelper.getNetworkInfo(context))
            appendLine(AdvancedControlHelper.getUptime())
            appendLine(AdvancedControlHelper.getBrightness(context))
            appendLine()
            appendLine("📟 *Device*: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine("🤖 *Android*: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
        }.trimEnd()
    }

    private fun getBatteryStatus(context: Context): String {
        return try {
            val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
                context.registerReceiver(null, ifilter)
            }

            val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1

            val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
            val isCharging: Boolean = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            val temperature = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, -1) ?: -1
            val voltage = batteryStatus?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1

            val batteryPct = if (level != -1 && scale != -1) {
                level * 100 / scale.toFloat()
            } else {
                -1f
            }

            val chargeStatus = if (isCharging) " (Charging ⚡)" else ""
            val tempStr = if (temperature > 0) " • ${temperature / 10.0}°C" else ""
            val voltStr = if (voltage > 0) " • ${voltage}mV" else ""

            "${batteryPct.toInt()}%$chargeStatus$tempStr$voltStr"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getStorageStatus(): String {
        return try {
            val path = Environment.getDataDirectory()
            val stat = StatFs(path.path)

            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalSpace = totalBlocks * blockSize
            val availableSpace = availableBlocks * blockSize

            val freeFormat = formatSize(availableSpace)
            val totalFormat = formatSize(totalSpace)

            "$freeFormat free of $totalFormat"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun formatSize(size: Long): String {
        if (size <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB", "TB")
        val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
        return DecimalFormat("# ,##0.#".replace(" ", "")).format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
