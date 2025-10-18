package com.project.reach.network.model

sealed class Packet {
    data class Message(val message: String): Packet()
    object TypingMessage: Packet()

    fun serialize(): ByteArray {
        return when (this) {
            is Message -> {
                "m:$message"
            }
            TypingMessage -> {
                "t:"
            }
        }.toByteArray()
    }

    companion object {
        fun deserialize(bytes: ByteArray): Packet? {
            val parts = bytes.decodeToString().split(':')
            val header = parts.getOrNull(0)

            return when (header) {
                "t" -> TypingMessage
                "m" -> {
                    val message = parts.getOrNull(1)
                    if (message?.isNotBlank() == true) Message(message) else null
                }
                else -> null
            }
        }
    }
}