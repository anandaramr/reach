package com.project.reach.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.reach.domain.contracts.ICallRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver: BroadcastReceiver() {

    @Inject
    lateinit var callRepository: ICallRepository
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onReceive(context: Context?, intent: Intent?) {
        val pendingResult = goAsync()
        scope.launch {
            try {
                when (intent?.action) {
                    "ACTION_ACCEPT_CALL" -> callRepository.acceptCall()
                    "ACTION_REJECT_CALL" -> callRepository.rejectCall()
                    "ACTION_END_CALL" -> callRepository.endCall()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}