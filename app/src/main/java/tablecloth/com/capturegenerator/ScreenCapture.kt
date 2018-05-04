package tablecloth.com.capturegenerator

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Handler
import android.util.Log
import java.util.*

/**
 * Created on 2018/05/04.
 */
class ScreenCapture(
        private val activity: Activity,
        private val uiHandler: Handler,
        private val captureWidthPixels: Int = activity.resources.displayMetrics.widthPixels,
        private val captureHeightPixels: Int = activity.resources.displayMetrics.heightPixels,
        private val captureDensityDpi: Int = activity.resources.displayMetrics.densityDpi,
        private val capturePixelFormat: Int = PixelFormat.RGBA_8888,
        private val bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888) {

    companion object {
        const val REQUEST_SCREEN_CAPTURE = 1
        const val VIRTUAL_DISPLAY_NAME = "ScreenCaptureDisplay"
    }

    private val TAG = "ScreenCapture"
    private val DEF_CAPTURE_START_MILLIS = -1L

    private val mediaProjectionManager: MediaProjectionManager = activity.getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    private val permissionIntent: Intent

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageReader: ImageReader? = null

    private var currentCaptureCount = 0
    private var captureStartMillis = DEF_CAPTURE_START_MILLIS
    private var captureConfig: CaptureConfig = CaptureConfig()

    private var onCaptureCallback: ((captureBitmap: Bitmap?, result: String) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null

    private var isCaptureAcive = false

    class CaptureConfig(
            val startDurationMillis: Long = CaptureConfig.START_DURATION_ASAP,
            val capturePeriodMillis: Long = CaptureConfig.CAPTURE_PERIOD_5_FPS,
            val stopDurationMillis: Long = CaptureConfig.STOP_DURATION_NO_LIMIT,
            val maxCaptureCount: Int = CaptureConfig.MAX_CAPTURE_COUNT_NO_LIMIT) {

        constructor(startDurationMillis: Long, capturePeriodMillis: Long, maxCaptureCount: Int)
                : this(startDurationMillis, capturePeriodMillis, CaptureConfig.STOP_DURATION_NO_LIMIT, maxCaptureCount)

        constructor(startDurationMillis: Long, capturePeriodMillis: Long, stopDurationMillis: Long)
                : this(startDurationMillis, capturePeriodMillis, stopDurationMillis, CaptureConfig.MAX_CAPTURE_COUNT_NO_LIMIT)

        companion object {
            val CAPTURE_PERIOD_60_FPS = 1000L / 60
            val CAPTURE_PERIOD_45_FPS = 1000L / 45
            val CAPTURE_PERIOD_30_FPS = 1000L / 30
            val CAPTURE_PERIOD_20_FPS = 1000L / 20
            val CAPTURE_PERIOD_10_FPS = 1000L / 10
            val CAPTURE_PERIOD_5_FPS = 1000L / 5
            val CAPTURE_PERIOD_1_FPS = 1000L / 1
            val MAX_CAPTURE_COUNT_NO_LIMIT = -1
            val START_DURATION_ASAP = 0L
            val STOP_DURATION_NO_LIMIT = -1L
        }
    }

    init {
        permissionIntent = mediaProjectionManager.createScreenCaptureIntent()
    }

    fun requestCapturePermission() {
        activity.startActivityForResult(permissionIntent, REQUEST_SCREEN_CAPTURE)
        onCaptureCallback = null
        onCompleteCallback = null
    }

    fun startCapture(
            captureCallback: (captureBitmap: Bitmap?, result: String) -> Unit,
            completeCallback: (() -> Unit)? = null,
            config: CaptureConfig = CaptureConfig()) {
        stopCapture()
        captureConfig = config
        currentCaptureCount = 0
        onCaptureCallback = captureCallback
        onCompleteCallback = completeCallback
        activity.startActivityForResult(permissionIntent, REQUEST_SCREEN_CAPTURE)
    }

    fun stopCapture() {
        virtualDisplay?.release()
        isCaptureAcive = false
    }

    fun generateCaptureBitmap(): Pair<Bitmap?, String> {
        val image = imageReader?.acquireLatestImage()
        image ?: return Pair(null, "Failed to acquire latest image from ImageReader.")

        val plane = image.planes[0]
        val buffer = plane.buffer
        val pixelStride = plane.pixelStride
        val rowStride = plane.rowStride
        val rowPadding = rowStride - pixelStride * captureWidthPixels

        val bitmap = Bitmap.createBitmap(
                captureWidthPixels + rowPadding / pixelStride,
                captureHeightPixels,
                bitmapConfig)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        return Pair(bitmap, "Success")
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Pair<Boolean, String> {
        if(requestCode != REQUEST_SCREEN_CAPTURE) {
            return Pair(false, "Request code is not REQUEST_SCREEN_CAPTURE. Request code is $requestCode.")
        }

        if(resultCode != Activity.RESULT_OK) {
            return Pair(false, "Result code is not Activity.RESULT_OK. Result code is $resultCode.")
        }

        // Stop current capture before reset.
        stopCapture()

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
        if(mediaProjection == null) {
            return Pair(false, "Failed to get media projection.")
        }

        initializeCaptureDisplay()

        if(onCaptureCallback != null) {
            registerNextCapture()
        }

        return Pair(true, "Success get permission for screen capture.")
    }

    private fun registerNextCapture() {
        if(onCaptureCallback == null) {
            Log.e(TAG, "capture callback needs to be assigned before registering capture.")
            return
        }

        if(!isCaptureAcive) {
            Log.e(TAG, "capture callback called when capture is not active.")
            return
        }

        // Initialize start time millis, if not set.
        if(captureStartMillis == DEF_CAPTURE_START_MILLIS) {
            captureStartMillis = Calendar.getInstance().timeInMillis
        }

        val delay =
                if(currentCaptureCount == 0) { captureConfig.startDurationMillis }
                else { captureConfig.capturePeriodMillis }

        // Register
        uiHandler.postDelayed({

            // Finish before capturing if capture is not currently active.
            if(!isCaptureAcive) {
                stopCapture()
                return@postDelayed
            }

            // Generate screen capture.
            val result = generateCaptureBitmap()
            onCaptureCallback!!(result.first, result.second)

            // Finish if count reaches maximum count.
            currentCaptureCount++
            if(captureConfig.maxCaptureCount != CaptureConfig.MAX_CAPTURE_COUNT_NO_LIMIT
                    && currentCaptureCount >= captureConfig.maxCaptureCount) {
                onCompleteCallback?.invoke()
                stopCapture()
                return@postDelayed
            }

            // Finish if time reaches stop duration.
            val currentMillis = Calendar.getInstance().timeInMillis
            if(captureConfig.stopDurationMillis != CaptureConfig.STOP_DURATION_NO_LIMIT
                    && currentMillis - captureStartMillis >= captureConfig.stopDurationMillis) {
                onCompleteCallback?.invoke()
                stopCapture()
                return@postDelayed
            }

            // Continue registering next capture.
            registerNextCapture()

        }, delay)
    }

    private fun initializeCaptureDisplay(): Pair<Boolean, String> {
        imageReader = ImageReader.newInstance(
                captureWidthPixels,
                captureHeightPixels,
                capturePixelFormat,
                2)
        if(imageReader == null) {
            return Pair(false, "Failed to create instance of ImageReader.")
        }

        virtualDisplay = mediaProjection?.createVirtualDisplay(
                VIRTUAL_DISPLAY_NAME,
                captureWidthPixels,
                captureHeightPixels,
                captureDensityDpi,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader?.surface,
                null,
                null)
        if(virtualDisplay == null) {
            return Pair(false, "Failed to create instance of VirtualDisplay.")
        }

        isCaptureAcive = true

        return Pair(true, "Success initialize capture display.")
    }
}
