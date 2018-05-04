package tablecloth.com.capturegenerator

import android.content.Intent
import android.graphics.Bitmap
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*

// http://techbooster.org/android/application/17026/
class MainActivity : AppCompatActivity() {

    private lateinit var screenCapture: ScreenCapture
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        screenCapture = ScreenCapture(this, Handler())
        screenCapture.requestCapturePermission()

        capture.setOnClickListener({
            screenCapture.startCapture(
                    captureCallback = { captureBitmap: Bitmap?, result: String ->
                        if(captureBitmap == null) {
                            Log.e(TAG, "Capture bitmap is null. Result: $result")
                            return@startCapture
                        }
                        Log.d(TAG, "Capture success. Result: $result")
                        capture_image.setImageBitmap(captureBitmap)
                    },
                    completeCallback = {
                        Log.d(TAG, "Capture complete.")
                    },
                    config = ScreenCapture.CaptureConfig(100L, 3000L, 3))
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        screenCapture.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        screenCapture.stopCapture()
        super.onDestroy()
    }
}
