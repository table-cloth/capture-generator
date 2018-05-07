package tablecloth.com.capturegenerator

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException

/**
 * Created on 2018/05/04.
 */
class FileUtil {

    companion object {

        private const val DEF_SHARED_PREFERENCES = "DefSharedPreferences"
        private const val DEF_STR = ""

        // https://qiita.com/kamedon39/items/8867a818768ccd2d0cda
        fun save2SharedPref(
                context: Context,
                key: String,
                value: Bitmap,
                compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG) {

            val byteArrayOutputStream = ByteArrayOutputStream()
            value.compress(compressFormat, 100, byteArrayOutputStream)
            val bitmapString = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT)

            save2SharedPref(context, key, bitmapString)
        }

        fun save2SharedPref(context: Context, key: String, value: String) {
            val sharedPrefEditor = getDefSharedPrefEditor(context)
            sharedPrefEditor.putString(key, value)
            sharedPrefEditor.apply()
        }

        fun loadSharedPrefString(context: Context, key: String, defValue: String = DEF_STR): String {
            val sharedPref = getDefSharedPref(context)
            return sharedPref.getString(key, defValue)
        }

        fun loadSharedPrefBitmap(context: Context, key: String, bitmapConfig: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? {
            val bitmapString = loadSharedPrefString(context, key, DEF_STR)
            if(bitmapString == DEF_STR) {
                return null
            }

            val bitmapOptions = BitmapFactory.Options()
            bitmapOptions.inPreferredConfig = bitmapConfig

            val byteData = Base64.decode(bitmapString, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(byteData, 0, byteData.size, bitmapOptions)
        }

        private fun getDefSharedPref(context: Context, accessMode: Int = Context.MODE_PRIVATE): SharedPreferences {
            return context.getSharedPreferences(DEF_SHARED_PREFERENCES, accessMode)
        }

        private fun getDefSharedPrefEditor(context: Context, accessMode: Int = Context.MODE_PRIVATE): SharedPreferences.Editor {
            return getDefSharedPref(context, accessMode).edit()
        }

        fun saveBitmapInternal(
                context: Context,
                fileName: String,
                bitmap: Bitmap,
                compressFormat: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG)
                : Pair<Boolean, String> {

            try {
                val byteArrOutputStream = ByteArrayOutputStream()
                val fileOutputStream = context.openFileOutput(fileName, Context.MODE_PRIVATE)

                bitmap.compress(compressFormat, 100, byteArrOutputStream)
                fileOutputStream.write(byteArrOutputStream.toByteArray())
                fileOutputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
                return Pair(false, e.message.toString())
            }

            return Pair(true, "Success")
        }

        fun loadBitmapInternal(context: Context, fileName: String): Pair<Bitmap?, String> {
            var bitmap: Bitmap? = null
            try {
                val bufferedInputStream = BufferedInputStream(context.openFileInput(fileName))
                bitmap = BitmapFactory.decodeStream(bufferedInputStream)
            } catch (e: IOException) {
                e.printStackTrace()
                return Pair(bitmap, e.message.toString())
            }
            return Pair(bitmap, "Success")
        }
    }
}
