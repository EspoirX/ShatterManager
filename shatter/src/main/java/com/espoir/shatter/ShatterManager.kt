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
    val shatterCache = ShatterCache()

    /**
     * 用来保存数据，方便各个Shatter获取
     */
    internal val dataSaveMap = hashMapOf<String, Any?>()

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

    open fun saveData(key: String, value: Any?) {
        dataSaveMap[key] = value
    }

    open fun <T> getSaveData(key: String): T? {
        return dataSaveMap.get(key) as? T?
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
        shatterCache.putShatter(shatter)
    }

    fun addShatter(shatter: Shatter) = apply {
        shatter.shatterManager = this
        shatter.attachActivity(activity)
        shatters.add(shatter)
        shatterCache.putShatter(shatter)
    }

    fun start() {
        shatters.forEach {
            it.onShatterCreate()
        }
    }

    fun remove(shatter: Shatter) {
        shatterCache.removeShatter(shatter.getTag())
        shatters.remove(shatter)
    }

    inline fun <reified T> findShatter(clazz: Class<T>): T? {
        if (clazz.isInterface) {
            val tag = clazz.simpleName
            var shatter = shatterCache.getShatter(tag)
            if (shatter == null) {
                val pair = shatterCache.cacheMap.anyMap { it.value is T } ?: return null
                shatter = pair.second
                shatterCache.cacheMap[tag] = shatter
            }
            return shatter as? T?
        } else {
            val tag = clazz.simpleName
            val shatter = shatterCache.getShatter(tag)
            return shatter as? T?
        }
    }

    fun destroy() {
        shatterCache.clear()
        dataSaveMap.clear()
        shatters.clear()
    }
}

inline fun <K, V> Map<out K, V>.anyMap(predicate: (Map.Entry<K, V>) -> Boolean): Pair<K, V>? {
    if (isEmpty()) return null
    for (element in this) if (predicate(element)) return Pair(element.key, element.value)
    return null
}