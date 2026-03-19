package com.project.reach.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.project.reach.domain.contracts.ICallRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CallActionReceiver: BroadcastReceiver() {

    @Inject
    lateinit var callRepository: ICallRepository
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            "ACTION_ACCEPT_CALL" -> {
                callRepository.acceptCall()
            }

            "ACTION_REJECT_CALL" -> {
                callRepository.rejectCall()
            }

            "ACTION_END_CALL" -> {
                callRepository.endCall()
            }
        }
    }
}