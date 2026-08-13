package com.example.telegrambot.helpers

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import com.example.telegrambot.TelegramBotApplication
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ScreenshotHelper {
    fun takeScreenshot(context: Context, onResult: (File?) -> Unit) {
        val activity = TelegramBotApplication.getActiveActivity()
        if (activity != null) {
            captureActivityView(activity, onResult)
        } else {
            generateStatusDashboardCard(context, onResult)
        }
    }

    private fun captureActivityView(activity: Activity, onResult: (File?) -> Unit) {
        Handler(Looper.getMainLooper()).post {
            try {
                val view = activity.window.decorView.rootView
                val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                view.draw(canvas)

                val file = File(activity.cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                bitmap.recycle()
                onResult(file)
            } catch (e: Exception) {
                e.printStackTrace()
                generateStatusDashboardCard(activity, onResult)
            }
        }
    }

    private fun generateStatusDashboardCard(context: Context, onResult: (File?) -> Unit) {
        Thread {
            try {
                val width = 1080
                val height = 1920
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)

                val bgPaint = Paint().apply {
                    color = Color.parseColor("#121212")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

                val accentPaint = Paint().apply {
                    color = Color.parseColor("#2196F3")
                    style = Paint.Style.FILL
                }
                canvas.drawRect(0f, 0f, width.toFloat(), 150f, accentPaint)

                val titlePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 64f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                }
                canvas.drawText("TELEGRAM BOT MONITOR", 80f, 250f, titlePaint)

                val subPaint = Paint().apply {
                    color = Color.parseColor("#888888")
                    textSize = 36f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif", android.graphics.Typeface.NORMAL)
                }
                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                canvas.drawText("Generated Snapshot • ${sdf.format(Date())}", 80f, 310f, subPaint)

                val cardPaint = Paint().apply {
                    color = Color.parseColor("#1E1E1E")
                    style = Paint.Style.FILL
                }
                val borderPaint = Paint().apply {
                    color = Color.parseColor("#333333")
                    style = Paint.Style.STROKE
                    strokeWidth = 4f
                }

                canvas.drawRoundRect(80f, 380f, 1000f, 680f, 24f, 24f, cardPaint)
                canvas.drawRoundRect(80f, 380f, 1000f, 680f, 24f, 24f, borderPaint)

                val labelPaint = Paint().apply {
                    color = Color.parseColor("#4CAF50")
                    textSize = 48f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                }
                canvas.drawText("🟢 BOT STATE: RUNNING", 130f, 470f, labelPaint)

                val textPaint = Paint().apply {
                    color = Color.parseColor("#E0E0E0")
                    textSize = 38f
                    isAntiAlias = true
                }
                canvas.drawText("Mode: Polling (Updates via Long Polling)", 130f, 540f, textPaint)
                canvas.drawText("Platform: Android Background Service", 130f, 600f, textPaint)

                canvas.drawRoundRect(80f, 730f, 1000f, 1230f, 24f, 24f, cardPaint)
                canvas.drawRoundRect(80f, 730f, 1000f, 1230f, 24f, 24f, borderPaint)

                val cardTitlePaint = Paint().apply {
                    color = Color.parseColor("#2196F3")
                    textSize = 44f
                    isAntiAlias = true
                    typeface = android.graphics.Typeface.create("sans-serif-medium", android.graphics.Typeface.BOLD)
                }
                canvas.drawText("💾 SYSTEM DIAGNOSTICS", 130f, 810f, cardTitlePaint)

                val battery = DeviceStatusHelper.getStatusMessage(context).replace("*", "")
                val lines = battery.split("\n").filter { it.isNotEmpty() }
                var yPos = 890f
                for (line in lines) {
                    canvas.drawText(line, 130f, yPos, textPaint)
                    yPos += 70f
                }

                val avatarPaint = Paint().apply {
                    color = Color.parseColor("#2196F3")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(540f, 1500f, 160f, avatarPaint)

                val innerAvatarPaint = Paint().apply {
                    color = Color.parseColor("#121212")
                    style = Paint.Style.FILL
                    isAntiAlias = true
                }
                canvas.drawCircle(540f, 1500f, 150f, innerAvatarPaint)

                val facePaint = Paint().apply {
                    color = Color.WHITE
                    textSize = 100f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("🤖", 540f, 1535f, facePaint)

                val footerPaint = Paint().apply {
                    color = Color.parseColor("#666666")
                    textSize = 32f
                    isAntiAlias = true
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("Secure Companion Agent Active Control Hub", 540f, 1750f, footerPaint)

                val file = File(context.cacheDir, "screenshot_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
                }
                bitmap.recycle()
                onResult(file)
            } catch (e: Exception) {
                e.printStackTrace()
                onResult(null)
            }
        }.start()
    }
}
