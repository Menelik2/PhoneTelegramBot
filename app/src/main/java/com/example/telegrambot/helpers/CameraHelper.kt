package com.example.telegrambot.helpers

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer

object CameraHelper {
    fun takePhoto(context: Context, useFront: Boolean, onPhotoTaken: (File?) -> Unit) {
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var cameraId: String? = null
        
        try {
            for (id in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing == (if (useFront) CameraCharacteristics.LENS_FACING_FRONT else CameraCharacteristics.LENS_FACING_BACK)) {
                    cameraId = id
                    break
                }
            }
            if (cameraId == null) {
                cameraId = cameraManager.cameraIdList.firstOrNull()
            }
            if (cameraId == null) {
                onPhotoTaken(null)
                return
            }

            val handlerThread = HandlerThread("CameraBackground").apply { start() }
            val handler = Handler(handlerThread.looper)

            val imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)

            cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    val captureBuilder = camera.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                    captureBuilder.addTarget(imageReader.surface)
                    captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                    val file = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
                    
                    imageReader.setOnImageAvailableListener({ reader ->
                        val image = reader.acquireLatestImage()
                        val buffer: ByteBuffer = image.planes[0].buffer
                        val bytes = ByteArray(buffer.capacity())
                        buffer.get(bytes)
                        try {
                            FileOutputStream(file).use { it.write(bytes) }
                            image.close()
                            camera.close()
                            handlerThread.quitSafely()
                            onPhotoTaken(file)
                        } catch (e: Exception) {
                            camera.close()
                            handlerThread.quitSafely()
                            onPhotoTaken(null)
                        }
                    }, handler)

                    // Deprecated createCaptureSession in API 30+, but simple for API 21+
                    camera.createCaptureSession(listOf(imageReader.surface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                session.capture(captureBuilder.build(), null, handler)
                            } catch (e: CameraAccessException) {
                                camera.close()
                                onPhotoTaken(null)
                            }
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            camera.close()
                            onPhotoTaken(null)
                        }
                    }, handler)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                    onPhotoTaken(null)
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    camera.close()
                    onPhotoTaken(null)
                }
            }, handler)
        } catch (e: Exception) {
            e.printStackTrace()
            onPhotoTaken(null)
        }
    }
}
