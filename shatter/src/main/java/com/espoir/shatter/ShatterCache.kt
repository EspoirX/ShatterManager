package com.espoir.shatter

class ShatterCache {
    private val cacheMap = hashMapOf<String, Shatter>()

    fun putShatter(shatter: Shatter) {
        cacheMap[shatter.getTag()] = shatter
    }

    fun getShatter(tag: String): Shatter? {
        return cacheMap[tag]
    }

    fun removeShatter(tag: String) {
        cacheMap.remove(tag)
    }

    fun clear(){
        cacheMap.clear()
    }
}