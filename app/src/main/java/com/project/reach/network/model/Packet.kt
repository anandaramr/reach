package com.project.reach.network.model

import com.reach.project.core.serialization.ReachPacket

sealed class Packet {
    data class Message(
        override val uuid: String,
        val message: String,
        val timeStamp: Long = System.currentTimeMillis()
    ) : Packet()

    data class Typing(
        override val uuid: String
    ) : Packet()

    abstract val uuid: String

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
                    senderUuid = packet.uuid
                    payload = packet.message
                    timestamp = packet.timeStamp
                }
                is Packet.Typing -> {
                    type = TYPE_TYPING
                    senderUuid = packet.uuid
                }
            }
        }
        return builder.build().toByteArray()
    }

    fun deserialize(bytes: ByteArray): Packet {
        val proto = ReachPacket.parseFrom(bytes)
        return when (proto.type) {
            TYPE_MESSAGE -> Packet.Message(
                uuid = proto.senderUuid,
                message = proto.payload,
                timeStamp = proto.timestamp
            )
            TYPE_TYPING -> Packet.Typing(
                uuid = proto.senderUuid
            )
            else -> throw IllegalArgumentException("Unknown packet type: ${proto.type}")
        }
    }
}