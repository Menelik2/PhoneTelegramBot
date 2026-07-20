package com.example.telegrambot.helpers

import android.content.Context
import android.provider.MediaStore
import java.io.File

object GalleryHelper {
    fun getLatestPhoto(context: Context): File? {
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            )
            
            cursor?.use {
                if (it.moveToFirst()) {
                    val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    val path = it.getString(dataIndex)
                    return File(path)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
}
