package com.project.reach.network.model

import com.project.reach.core.exceptions.UnknownSourceException

sealed class Packet {
    data class Message(
        override val senderId: String,
        val messageId: String,
        val senderUsername: String,
        val message: String,
        val timeStamp: Long
    ): Packet()

    data class Typing(
        override val senderId: String
    ): Packet()

    data class Hello(
        override val senderId: String,
        val senderUsername: String
    ): Packet()

    data class Heartbeat(
        override val senderId: String,
        val senderUsername: String
    ): Packet()

    data class GoodBye(
        override val senderId: String
    ): Packet()

    abstract val senderId: String

    /**
     * Serializes the packet and returns a [ByteArray] object
     */
    fun serialize(): ByteArray = PacketSerializer.serialize(this)

    companion object {
        /**
         * Deserializes given bytes and returns [Packet] object
         *
         * @throws IllegalArgumentException if the provided data cannot be deserialized
         * because it does not conform to the expected packet format.
         *
         * @throws UnknownSourceException if the received packet is from an unknown service
         */
        fun deserialize(bytes: ByteArray): Packet = PacketSerializer.deserialize(bytes)
    }
}