package tablecloth.com.capturegenerator

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import java.util.*

/**
 * Created on 2018/04/20.
 */
class CaptureService: Service() {

//    companion object {
//        private val TAG = CaptureService::class.qualifiedName
//        val ACTION_ENABLE_CAPTURE = "enable_capture"
//    }
//
//    private val notificationId = Random().nextInt()
//
//    private val capture = Capture(this)
//
//    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        if(intent != null) {
//            when(intent.action) {
//                ACTION_ENABLE_CAPTURE -> enableCapture()
//            }
//        }
//        return Service.START_STICKY
//    }
//
//    private fun enableCapture() {
//        if(MainActivity.projection == null) {
//            Log.d(TAG, "startActivity(MainACtivity)")
//            val intent = Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            startActivity(intent)
//        } else {
//            onEnableCapture()
//        }
//    }
//
//    private fun onEnableCapture() {
//        MainActivity.projection?.run {
//            capture.run(this) {
//                capture.stop()
//                // Save bitmap
//
//                disableCapture()
//            }
//        }
//    }
//
//    private fun disableCapture() {
//        capture.stop()
//        MainActivity.projection = null
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        disableCapture()
//    }






    override fun onBind(p0: Intent?): IBinder {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}