package com.project.reach.network.model

import com.reach.project.core.serialization.ReachPacket

sealed class Packet {
    data class Message(
        override val userId: String,
        val username: String,
        val message: String,
        val timeStamp: Long = System.currentTimeMillis()
    ) : Packet()

    data class Typing(
        override val userId: String
    ) : Packet()

    abstract val userId: String

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
         */
        fun deserialize(bytes: ByteArray): Packet = PacketSerializer.deserialize(bytes)
    }
}

private object PacketSerializer {
    private const val TYPE_MESSAGE = "message"
    private const val TYPE_TYPING = "typing"

    fun serialize(packet: Packet): ByteArray {
        val builder = ReachPacket.newBuilder().apply {
            when (packet) {
                is Packet.Message -> {
                    type = TYPE_MESSAGE
                    senderUuid = packet.userId
                    senderUsername = packet.username
                    payload = packet.message
                    timestamp = packet.timeStamp
                }
                is Packet.Typing -> {
                    type = TYPE_TYPING
                    senderUuid = packet.userId
                }
            }
        }
        return builder.build().toByteArray()
    }

    fun deserialize(bytes: ByteArray): Packet {
        val proto = ReachPacket.parseFrom(bytes)
        return when (proto.type) {
            TYPE_MESSAGE -> Packet.Message(
                userId = proto.senderUuid,
                username = proto.senderUsername,
                message = proto.payload,
                timeStamp = proto.timestamp
            )
            TYPE_TYPING -> Packet.Typing(
                userId = proto.senderUuid
            )
            else -> throw IllegalArgumentException("Unknown packet type: ${proto.type}")
        }
    }
}