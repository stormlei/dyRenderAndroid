package com.qpsoft.checkrender

import android.app.Application
import com.blankj.utilcode.util.LogUtils
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CRMApplication : Application() {

    companion object {

        lateinit var instance: CRMApplication
            private set

    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        // utilcode
        //Utils.init(this)
        LogUtils.getConfig().isLogSwitch = true
    }
}