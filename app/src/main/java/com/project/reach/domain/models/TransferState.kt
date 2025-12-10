package com.project.reach.domain.models

sealed interface TransferState {
    data class Progress(val currentBytes: Long): TransferState
    object Preparing: TransferState
    object Paused: TransferState
    object Complete: TransferState
}