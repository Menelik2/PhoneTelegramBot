package com.example.telegrambot.helpers

import android.content.Context
import android.media.AudioManager
import android.media.RingtoneManager

object RemoteControlHelper {
    fun ringDevice(context: Context) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING)
            audioManager.setStreamVolume(AudioManager.STREAM_RING, maxVolume, 0)

            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
            val ringtone = RingtoneManager.getRingtone(context, uri)
            ringtone.play()
            
            // Stop after 15 seconds
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                if (ringtone.isPlaying) {
                    ringtone.stop()
                }
            }, 15000)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
