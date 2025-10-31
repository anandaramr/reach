package com.project.reach.util

import android.util.Log
import com.project.reach.BuildConfig

fun debug(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("DBG", message)
    }
}

