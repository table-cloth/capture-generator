package tablecloth.com.capturegenerator

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.util.Log
import android.view.Surface
import kotlinx.android.synthetic.main.activity_main.*

// http://techbooster.org/android/application/17026/
class MainActivity : AppCompatActivity() {

//    companion object {
//        private const val REQUEST_CAPTURE = 1
//        var projection: MediaProjection?  = null
//    }
//
//    private lateinit var mediaProjectionManager: MediaProjectionManager

    private val TAG = "ScreenCapture"
    private val REQUEST_SCREEN_CAPTURE = 1

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var permissionIntent: Intent

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null

    private var imageReader: ImageReader? = null
    private var uiHandler: Handler? = null


//    private lateinit var screenCapture: ScreenCapture = null

    init {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        uiHandler = Handler()

        mediaProjectionManager = getSystemService(android.content.Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        permissionIntent = mediaProjectionManager.createScreenCaptureIntent()

        Log.d(TAG, "Start activity.")


        Log.d(TAG, "Start permission intent.")
        startActivityForResult(permissionIntent, REQUEST_SCREEN_CAPTURE)

//        screenCapture.initialize(onActivityResult = )

        capture_image.setOnClickListener({ _ ->
            Log.d(TAG, "Start permission intent by image.")
            startActivityForResult(permissionIntent, REQUEST_SCREEN_CAPTURE)
        })

        capture.setOnClickListener({ _ ->
//            capture_image.setImageBitmap(updateCaptureImage())

            Log.d(TAG, "Start permission intent by button.")
            startActivityForResult(permissionIntent, REQUEST_SCREEN_CAPTURE)
        })


//        mediaProjectionManager = getSystemService(Service.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
//        startActivityForResult(mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CAPTURE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode != REQUEST_SCREEN_CAPTURE) {
            return
        }

        if(resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "SUCCESSSSSSS")
            Snackbar.make(activity_root, "Permission is ON", Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(activity_root, "Permission is OFF", Snackbar.LENGTH_SHORT).show()
            return
        }

        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, intent)

        if(mediaProjection == null) {
            Snackbar.make(activity_root, "mediaProjection is NULL...", Snackbar.LENGTH_SHORT).show()
            return
        }


        // Display info
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        // Setup virtual display
        imageReader = ImageReader.newInstance(
                width, height, PixelFormat.RGBA_8888, 2)
        virtualDisplay = mediaProjection!!.createVirtualDisplay(
                "Capturing Display...!", width, height, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                imageReader!!.surface,
                null, null)

//        Handler().postDelayed(Runnable {
//            uiHandler!!.post(Runnable {
//                capture_image.setImageBitmap(updateCaptureImage())
//            })
//        }, 1000)

    }

    private fun updateCaptureImage(): Bitmap? {
//        capture_image
        val image = imageReader?.acquireLatestImage()
        if(image == null) {
            Log.e(TAG, "FAIL TO CAPUTRE IMAGE")
            Snackbar.make(activity_root, "Fail update capture image...", Snackbar.LENGTH_SHORT).show()
            return null
        }

        val metrics = resources.displayMetrics

        val planes = image.planes
        val buffer = planes[0].buffer

        val pixelStide = planes[0].pixelStride
        val rowStride = planes[0].rowStride
        val rowPadding = rowStride - pixelStide * metrics.widthPixels

        // Create bitmap from buffer
        val bitmap = Bitmap.createBitmap(
                metrics.widthPixels + rowPadding / pixelStide,
                metrics.heightPixels,
                Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        image.close()

        return bitmap
    }


    override fun onPause() {
        Log.d(TAG, "OnPause")
        Snackbar.make(activity_root, "Release virtual display.", Snackbar.LENGTH_SHORT).show()
        super.onPause()
    }

    override fun onDestroy() {
        virtualDisplay?.release()
        Log.d(TAG, "OnDestroy")
        Snackbar.make(activity_root, "Release virtual display.", Snackbar.LENGTH_SHORT).show()
        super.onDestroy()
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if(requestCode == REQUEST_CAPTURE) {
//            if(resultCode == Activity.RESULT_OK) {
//                projection = mediaProjectionManager.getMediaProjection(resultCode, data)
////                val intent = Intent(this, CaptureService::class.java)
////                startService(intent)
//                Toast.makeText(this, "capture start service", Toast.LENGTH_SHORT).show()
//            } else {
//                projection = null
//                Toast.makeText(this, "capture error", Toast.LENGTH_SHORT).show()
//            }
//        }
//        finish()
//    }
}
