package com.espoir.shatter

import android.app.Application
import android.content.Intent
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShatterManager(internal val activity: AppCompatActivity) : LifecycleEventObserver {

    internal val shatters = mutableListOf<Shatter>()
    private var activityWatcher = ShatterActivityWatcher(activity)
    private val newMsgFlow: MutableSharedFlow<ShatterEvent<*>> by lazy {
        MutableSharedFlow()
    }
    internal val cache = ShatterCache()

    companion object {
        fun init(application: Application) {
            GlobalShatterActivity.INSTANCE.init(application)
        }
    }

    init {
        activity.lifecycle.removeObserver(this)
        activity.lifecycle.addObserver(this)
        activityWatcher.install()
        activity.lifecycleScope.launch {
            newMsgFlow.collectLatest { event ->
                shatters.forEach { it.onShatterEvent(event.key, event.data) }
            }
        }
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        shatters.forEach { it.onStateChanged(source, event) }
        if (event == Lifecycle.Event.ON_DESTROY) {
            activityWatcher.onDestroy()
            activityWatcher.uninstall()
            destroy()
            source.lifecycle.removeObserver(this)
            return
        }
    }

    internal fun sendShatterEvent(key: String, data: Any? = null) {
        activity.lifecycleScope.launch {
            newMsgFlow.emit(ShatterEvent(key, data))
        }
    }

    fun onNewIntent(intent: Intent?) {
        activityWatcher.onNewIntent(intent)
    }

    fun onRestart() {
        activityWatcher.onRestart()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        activityWatcher.onActivityResult(requestCode, resultCode, data)
    }

    fun enableOnBackPressed(): Boolean {
        return shatters.find { !it.enableOnBackPressed() } == null
    }

    fun addShatter(@IdRes containViewId: Int, shatter: Shatter) = apply {
        shatter.shatterManager = this
        val containView = activity.findViewById<View>(containViewId)
        addShatter(containView, shatter)
    }

    fun addShatter(containView: View, shatter: Shatter) = apply {
        shatter.shatterManager = this
        shatter.containView = containView
        shatter.attachActivity(activity)
        shatters.add(shatter)
        cache.putShatter(shatter)
        shatter.attachChildShatter()
    }

    fun addShatter(shatter: Shatter) = apply {
        shatter.shatterManager = this
        shatter.attachActivity(activity)
        shatters.add(shatter)
        cache.putShatter(shatter)
        shatter.attachChildShatter()
    }

    fun remove(shatter: Shatter) {
        shatters.find { it.getTag() == shatter.getTag() }?.childShatters?.forEach {
            cache.removeShatter(it.getTag())
        }
        cache.removeShatter(shatter.getTag())
        shatters.remove(shatter)
    }

    fun <T : Shatter> findShatter(clazz: Class<T>): T? {
        val tag = clazz.simpleName
        val shatter = shatters.find { it.getTag() == tag } ?: return null
        return shatter as T
    }

    fun destroy() {
        cache.clear()
        shatters.forEach { it.childShatters.clear() }
        shatters.clear()
    }
}