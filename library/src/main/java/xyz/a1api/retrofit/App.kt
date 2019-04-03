package xyz.a1api.retrofit

import android.app.Application

/**
 * Created by Cat-x on 2019/4/3.
 * For RetrofitEx
 * Cat-x All Rights Reserved
 */
open class App : Application() {

    companion object {
        lateinit var app: App
            private set
    }


    override fun onCreate() {
        super.onCreate()
        app = this


    }

}