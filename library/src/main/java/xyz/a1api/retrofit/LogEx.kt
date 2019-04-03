package xyz.a1api.retrofit

import android.util.Log

/**
 * Created by Cat-x on 2019/1/19.
 * For ZQeducation2
 * Cat-x All Rights Reserved
 */
object LogEx {
    val TAG = "LogEx"
    var isDebug = BuildConfig.DEBUG

    fun i(tag: String, vararg msg: Any) {
        if (BuildConfig.DEBUG) {
            var string = "TAG = $tag : "
            for (any in msg) {
                string = "$string$any || "
            }
            Log.i(TAG, string)
        }
    }
}