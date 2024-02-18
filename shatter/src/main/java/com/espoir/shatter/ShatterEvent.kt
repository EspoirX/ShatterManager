package com.espoir.shatter

import java.io.Serializable

class ShatterEvent<T>(val key: String, val data: T? = null) : Serializable