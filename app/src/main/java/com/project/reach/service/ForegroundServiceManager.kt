package com.project.reach.service

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object ForegroundServiceManager {

    private val _isRunning = MutableStateFlow(false)
    val isRunningFlow = _isRunning.asStateFlow()

    fun setServiceState(state: Boolean) {
        _isRunning.value = state
    }

    fun isRunning() = isRunningFlow.value

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