package com.espoir.shattermanager

import android.app.Application
import com.espoir.shatter.ShatterManager

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        ShatterManager.init(this)
    }
}