package com.project.reach.network.model

sealed class Packet {
    data class Message(val message: String): Packet()
    object TypingMessage: Packet()

    fun serialize(): ByteArray {
        return when (this) {
            is Message -> {
                "m:$message".toByteArray()
            }
            TypingMessage -> {
                "t:".toByteArray()
            }
        }
    }
}