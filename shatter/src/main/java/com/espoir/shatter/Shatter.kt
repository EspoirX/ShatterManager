package com.espoir.shatter

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.coroutineScope
import androidx.viewbinding.ViewBinding

open class Shatter : ShatterLifecycleListener, LifecycleOwner {

    companion object {
        const val NO_LAYOUT = 0
    }

    var shatterManager: ShatterManager? = null
    var containView: View? = null
    private var act: AppCompatActivity? = null
    val activity: AppCompatActivity
        get() = if (act == null) {
            GlobalShatterActivity.INSTANCE.currentActivity as AppCompatActivity
        } else act!!

    val lifecycleScope: LifecycleCoroutineScope
        get() = lifecycle.coroutineScope

    @PublishedApi
    internal var viewBinding: ViewBinding? = null

    inline fun <reified B : ViewBinding> getBinding(): B {
        return if (viewBinding == null) {
            val method = B::class.java.getMethod("bind", View::class.java)
            val viewBinding = method.invoke(null, containView) as B
            this.viewBinding = viewBinding
            viewBinding
        } else {
            viewBinding as B
        }
    }

    fun attachActivity(activity: AppCompatActivity?) {
        onAttachActivity(activity)
        if (getLayoutResId() != NO_LAYOUT && containView != null) {
            if (containView is ViewGroup) {
                val view = LayoutInflater.from(activity).inflate(getLayoutResId(), null)
                (containView as ViewGroup).addView(view)
                containView = view
            }
        }
    }

    fun onAttachActivity(activity: AppCompatActivity?) {
        this.act = activity
    }

    fun finish() {
        this.activity.finish()
    }

    fun onShatterCreate() {
        onCreate(activity.intent)
        initView(containView, activity.intent)
        initData(activity.intent)
    }

    @LayoutRes
    open fun getLayoutResId(): Int = NO_LAYOUT

    open fun getTag(): String = this::class.java.simpleName

    inline fun <reified T> findShatter(clazz: Class<T>): T? {
        return shatterManager?.findShatter(clazz)
    }

    open fun saveData(key: String, value: Any?) {
        shatterManager?.saveData(key, value)
    }

    open fun <T> getSaveData(key: String): T? {
        return shatterManager?.getSaveData(key)
    }

    open fun sendShatterEvent(key: String, data: Any? = null) {
        shatterManager?.sendShatterEvent(key, data)
    }

    open fun onShatterEvent(key: String, data: Any?) {
    }

    open fun onCreate(intent: Intent?) {}

    open fun initView(view: View?, intent: Intent?) {}

    open fun initData(intent: Intent?) {}

    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    }

    override fun onNewIntent(intent: Intent?) {
    }

    override fun onSaveInstanceState(outState: Bundle?) {
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
    }

    override fun onStart() {
    }

    override fun onResume() {
    }

    override fun onPause() {
    }

    override fun onStop() {
    }

    override fun onRestart() {
    }

    override fun onDestroy() {
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    }

    override fun enableOnBackPressed(): Boolean {
        return true
    }

    open fun onSelfDestroy() {
    }

    fun startActivity(intent: Intent) {
        activity.startActivity(intent)
    }

    override fun getLifecycle(): Lifecycle {
        return activity.lifecycle
    }
}