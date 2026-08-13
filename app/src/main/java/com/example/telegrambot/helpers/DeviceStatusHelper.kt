package com.example.telegrambot.helpers

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.text.DecimalFormat

object DeviceStatusHelper {
    fun getStatusMessage(context: Context): String {
        return "📱 *Device Status*\n" +
                "\n🔋 *Battery*: ${getBatteryStatus(context)}" +
                "\n💾 *Storage*: ${getStorageStatus()}"
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
            
            val batteryPct = if (level != -1 && scale != -1) {
                level * 100 / scale.toFloat()
            } else {
                -1f
            }
            
            val chargeStatus = if (isCharging) " (Charging ⚡)" else ""
            "${batteryPct.toInt()}%$chargeStatus"
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
        return DecimalFormat("#,##0.#").format(size / Math.pow(1024.0, digitGroups.toDouble())) + " " + units[digitGroups]
    }
}
