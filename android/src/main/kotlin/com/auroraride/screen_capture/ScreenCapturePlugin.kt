package com.auroraride.screen_capture

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.NonNull
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding.OnSaveInstanceStateListener
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.RequiresApi
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class ScreenCapturePlugin :
    FlutterPlugin,
    MethodCallHandler,
    ActivityAware,
    OnSaveInstanceStateListener,
    PluginRegistry.ActivityResultListener {
    private lateinit var activity: Activity
    private lateinit var context: Context
    private lateinit var channel: MethodChannel
    private lateinit var result: Result
    private var mediaProjection: MediaProjection? = null

    companion object {
        const val CHANNEL_NAME = "screen_capture"
        const val REQUEST_MEDIA_PROJECTION = 1001
    }

    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(binding.binaryMessenger, CHANNEL_NAME)
        channel.setMethodCallHandler(this)
        context = binding.applicationContext
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

        binding.addOnSaveStateListener(this)
        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivityForConfigChanges() {
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity

        binding.addActivityResultListener(this)
    }

    override fun onDetachedFromActivity() {}

    override fun onSaveInstanceState(outState: Bundle) {}

    override fun onRestoreInstanceState(bundle: Bundle?) {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onMethodCall(call: MethodCall, result: Result) {
        this.result = result

        when (call.method) {
            "requestPermission" -> {
                requestPermission()
            }

            "takeCapture" -> {
                takeCapture(call, result)
            }

            else -> result.notImplemented()
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val manager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as? MediaProjectionManager
            manager?.let {
                activity.startActivityForResult(
                    it.createScreenCaptureIntent(),
                    REQUEST_MEDIA_PROJECTION
                )
            }
        } else {
            result.error(
                "UNSUPPORTED_VERSION",
                "Android version must be Lollipop or higher",
                null
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        var res = false

        when (requestCode) {
            REQUEST_MEDIA_PROJECTION -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    res = true
                }
                result.success(res)
            }

            else -> {}
        }

        return res
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun takeCapture(call: MethodCall, result: Result) {
        if (mediaProjection == null) {
//            result.error(
//                "NEED_REQUEST_PERMISSION",
//                "Must request permission before take capture",
//                null
//            )
//            return
            val mediaProjectionManager =
                context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            mediaProjection =
                mediaProjectionManager.getMediaProjection(Activity.RESULT_OK, activity.intent)
        }

        val region = call.arguments as Map<*, *>
        val x = region["x"] as Int
        val y = region["y"] as Int
        val width = region["width"] as Int
        val height = region["height"] as Int

        val imageReader = ImageReader.newInstance(width, height, ImageFormat.RGB_565, 2)

        val virtualDisplay = mediaProjection?.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            context.resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader.surface,
            null,
            null,
        )

        Handler(Looper.getMainLooper()).postDelayed({
            val image = imageReader.acquireLatestImage()
            val buffer = image!!.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            image.close()
            virtualDisplay?.release()

            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

            val croppedBitmap =
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size).crop(x, y, width, height)

            val outputStream = ByteArrayOutputStream()
            croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)

            val byteArray = outputStream.toByteArray()
            result.success(byteArray)
        }, 100)
    }
}

private fun Bitmap.crop(left: Int, top: Int, width: Int, height: Int): Bitmap {
    return Bitmap.createBitmap(this, left, top, width, height)
}
