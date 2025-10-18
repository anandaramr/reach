package com.project.reach.service

import android.content.Context
import android.content.Intent

object ForegroundServiceManager {
    fun startService(context: Context) {
        startIntent(context, ForegroundService.ACTION_START)
    }

    fun stopService(context: Context) {
        startIntent(context, ForegroundService.ACTION_STOP)
    }

    private fun startIntent(context: Context, action: String) {
        Intent(context, ForegroundService::class.java).also {
            it.action = action
            context.startService(it)
        }
    }
}