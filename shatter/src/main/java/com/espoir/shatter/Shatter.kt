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

    internal val childShatters = mutableListOf<Shatter>()

    fun addChildShatters(shatter: Shatter) = apply {
        childShatters.add(shatter)
    }

    fun addChildShatters(containView: View, shatter: Shatter) = apply {
        shatter.containView = containView
        childShatters.add(shatter)
    }

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
        onShatterCreate()
    }

    fun attachChildShatter() {
        childShatters.forEach {
            it.shatterManager = shatterManager
            it.attachActivity(activity)
            ShatterCache.putShatter(it)
        }
    }

    fun onAttachActivity(activity: AppCompatActivity?) {
        this.act = activity
    }

    fun finish() {
        this.activity.finish()
    }

    private fun onShatterCreate() {
        onCreate(activity.intent)
        initView(containView, activity.intent)
        initData(activity.intent)
    }

    @LayoutRes
    open fun getLayoutResId(): Int = NO_LAYOUT

    open fun getTag(): String = this::class.java.simpleName

    open fun <T : Shatter> findShatter(clazz: Class<T>): T? {
        val tag = clazz.simpleName
        val shatter = ShatterCache.getShatter(tag)
        if (shatter != null) {
            return shatter as T
        }
        return null
    }

    open fun sendShatterEvent(key: String, data: Any? = null) {
        shatterManager?.sendShatterEvent(key, data)
    }

    open fun onShatterEvent(key: String, data: Any?) {
        childShatters.forEach { it.onShatterEvent(key, data) }
    }

    open fun onCreate(intent: Intent?) {}

    open fun initView(view: View?, intent: Intent?) {}

    open fun initData(intent: Intent?) {}

    fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
    }

    override fun onNewIntent(intent: Intent?) {
        childShatters.forEach { it.onNewIntent(intent) }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        childShatters.forEach { it.onSaveInstanceState(outState) }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        childShatters.forEach { it.onRestoreInstanceState(savedInstanceState) }
    }

    override fun onStart() {
        childShatters.forEach { it.onStart() }
    }

    override fun onResume() {
        childShatters.forEach { it.onResume() }
    }

    override fun onPause() {
        childShatters.forEach { it.onPause() }
    }

    override fun onStop() {
        childShatters.forEach { it.onStop() }
    }

    override fun onRestart() {
        childShatters.forEach { it.onRestart() }
    }

    override fun onDestroy() {
        childShatters.forEach { it.onDestroy() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        childShatters.forEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun enableOnBackPressed(): Boolean {
        return childShatters.find { !it.enableOnBackPressed() } == null
    }

    open fun onSelfDestroy() {
        childShatters.forEach { it.onSelfDestroy() }
    }

    fun startActivity(intent: Intent) {
        activity.startActivity(intent)
    }

    override fun getLifecycle(): Lifecycle {
        return activity.lifecycle
    }
}