package com.espoir.shatter


import android.content.Intent
import android.os.Bundle

interface ShatterLifecycleListener {
    fun onNewIntent(intent: Intent?)

    fun onSaveInstanceState(outState: Bundle?)

    fun onRestoreInstanceState(savedInstanceState: Bundle?)

    fun onStart()

    fun onResume()

    fun onPause()

    fun onStop()

    fun onRestart()

    fun onDestroy()

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun enableOnBackPressed(): Boolean
}