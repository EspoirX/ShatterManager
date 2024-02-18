package com.espoir.shatter

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log

class ShatterActivityWatcher(val currAct: Activity) {
    companion object {
        const val TAG = "ShatterActivityWatcher"
    }

    private val lifecycleCallbacks = object : Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Log.i(TAG, "- onActivityCreated -")
            GlobalShatterActivity.INSTANCE.currentActivity = activity
        }

        override fun onActivityStarted(activity: Activity) {
            if (activity == currAct) {
                Log.i(TAG, "- onActivityStarted -")
                dispatch { it -> it.shatters.forEach { it.onStart() } }
            }
        }

        override fun onActivityResumed(activity: Activity) {
            GlobalShatterActivity.INSTANCE.currentActivity = activity
            if (activity == currAct) {
                Log.i(TAG, "- onActivityResumed -")
                dispatch { it -> it.shatters.forEach { it.onResume() } }
            }
        }

        override fun onActivityPaused(activity: Activity) {
            if (activity == currAct) {
                Log.i(TAG, "- onActivityPaused -")
                dispatch { it -> it.shatters.forEach { it.onPause() } }
            }
        }

        override fun onActivityStopped(activity: Activity) {
            if (activity == currAct) {
                Log.i(TAG, "- onActivityStopped -")
                dispatch { it -> it.shatters.forEach { it.onStop() } }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            if (activity == currAct) {
                Log.i(TAG, "- onActivitySaveInstanceState -")
                dispatch { it -> it.shatters.forEach { it.onSaveInstanceState(outState) } }
            }
        }

        override fun onActivityDestroyed(activity: Activity) {
            if (activity == currAct) {
                Log.i(TAG, "- onActivityDestroyed -")
                dispatch { it -> it.shatters.forEach { it.onDestroy() } }
            }
        }
    }

    fun onNewIntent(intent: Intent?) {
        Log.i(TAG, "- onNewIntent -")
        dispatch { it -> it.shatters.forEach { it.onNewIntent(intent) } }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i(TAG, "- onActivityResult -")
        dispatch { it -> it.shatters.forEach { it.onActivityResult(requestCode, resultCode, data) } }
    }

    fun onRestart() {
        Log.i(TAG, "- onRestart -")
        dispatch { it -> it.shatters.forEach { it.onRestart() } }
    }

    fun onDestroy() {
        Log.i(TAG, "- onDestroy -")
        GlobalShatterActivity.INSTANCE.currentActivity = null
        dispatch { it -> it.shatters.forEach { it.onDestroy() } }
    }

    fun dispatch(callback: (ShatterManager) -> Unit) {
        if (currAct is IShatterActivity) {
            val manager = currAct.getShatterManager()
            callback.invoke(manager)
        }
    }

    fun install() {
        GlobalShatterActivity.INSTANCE.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    fun uninstall() {
        GlobalShatterActivity.INSTANCE.unregisterActivityLifecycleCallbacks(lifecycleCallbacks)
    }
}