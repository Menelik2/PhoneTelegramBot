package com.example.telegrambot.helpers

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import java.io.File

object AudioRecordHelper {
    fun recordAudio(context: Context, durationMs: Long, onAudioRecorded: (File?) -> Unit) {
        try {
            val file = File(context.cacheDir, "audio_record_${System.currentTimeMillis()}.m4a")
            
            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            recorder.setOutputFile(file.absolutePath)
            
            recorder.prepare()
            recorder.start()
            
            Handler(Looper.getMainLooper()).postDelayed({
                try {
                    recorder.stop()
                    recorder.release()
                    onAudioRecorded(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    onAudioRecorded(null)
                }
            }, durationMs)
        } catch (e: Exception) {
            e.printStackTrace()
            onAudioRecorded(null)
        }
    }
}
