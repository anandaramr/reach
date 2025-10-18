package com.project.reach.domain.contracts

import kotlinx.coroutines.flow.StateFlow

interface IWifiController {
    val isActive: StateFlow<Boolean>

    fun close()
}