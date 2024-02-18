package com.espoir.shatter

import android.app.Activity
import android.app.Application

enum class GlobalShatterActivity {
    INSTANCE;

    var currentActivity: Activity? = null

    private lateinit var application: Application

    fun init(application: Application) {
        this.application = application
    }

    fun registerActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks) {
        this.application.registerActivityLifecycleCallbacks(callback)
    }


    fun unregisterActivityLifecycleCallbacks(callback: Application.ActivityLifecycleCallbacks) {
        this.application.unregisterActivityLifecycleCallbacks(callback)
    }
}