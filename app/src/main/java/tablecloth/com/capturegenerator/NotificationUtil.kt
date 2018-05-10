package tablecloth.com.capturegenerator

import android.annotation.TargetApi
import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.support.v4.app.NotificationCompat

/**
 * Created by admin on 2018/05/10.
 * https://qiita.com/chiiia12/items/7854187b3e7b44099700
 */
class NotificationUtil() {

    companion object {
//        val NOTIFICATION_ID = ""
//    }

        fun <T>register(context: Context, cls: Class<T>) {

//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            registerNotification26Over(context, cls)
//        } else {
            registerNotification(context, cls)
//        }
        }

        @TargetApi(Build.VERSION_CODES.O)
        private fun <T>registerNotification26Over(context: Context, cls: Class<T>) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.getNotificationChannel("..")
            val intent = Intent(context, cls)


        }

        private fun <T>registerNotification(context: Context, cls: Class<T>) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val intent = Intent(context, cls)
            val largeIconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_launcher_foreground)

            val builder = NotificationCompat.Builder(context)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setLargeIcon(largeIconBitmap)
                    .setTicker("Ticker")
                    .setContentTitle("Title")
                    .setContentText("Content")
                    .setAutoCancel(true)

            notificationManager.notify(0, builder.build())


        }
    }
}

class NotificationReceiver() {

}