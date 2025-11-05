package com.project.reach.network.model

import com.google.protobuf.InvalidProtocolBufferException
import com.project.reach.core.exceptions.UnknownSourceException
import com.reach.project.core.serialization.ReachPacket
import java.util.zip.DataFormatException

sealed class Packet {
    data class Message(
        override val senderId: String,
        val senderUsername: String,
        val message: String,
        val timeStamp: Long = System.currentTimeMillis()
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
         * @throws DataFormatException if the provided data cannot be deserialized
         * because it does not conform to the expected packet format.
         *
         * @throws UnknownSourceException if the received packet is from an unknown service
         *
         * @throws IllegalArgumentException if the parsed data contains unrecognizable values
         */
        fun deserialize(bytes: ByteArray): Packet = PacketSerializer.deserialize(bytes)
    }
}

private object PacketSerializer {
    fun serialize(packet: Packet): ByteArray {
        val builder = ReachPacket
            .newBuilder()
            .setServiceName(SERVICE_NAME)
            .apply {
                when (packet) {
                    is Packet.Message -> {
                        type = TYPE_MESSAGE
                        senderUuid = packet.senderId
                        senderUsername = packet.senderUsername
                        payload = packet.message
                        timestamp = packet.timeStamp
                    }

                    is Packet.Typing -> {
                        type = TYPE_TYPING
                        senderUuid = packet.senderId
                    }

                    is Packet.Heartbeat -> {
                        type = TYPE_HEARTBEAT
                        senderUuid = packet.senderId
                        senderUsername = packet.senderUsername
                    }

                    is Packet.Hello -> {
                        type = TYPE_HELLO
                        senderUuid = packet.senderId
                        senderUsername = packet.senderUsername
                    }

                    is Packet.GoodBye -> {
                        type = TYPE_GOODBYE
                        senderUuid = packet.senderId
                    }
                }
            }
        return builder.build().toByteArray()
    }

    fun deserialize(bytes: ByteArray): Packet {
        val proto = try {
            ReachPacket.parseFrom(bytes)
        } catch (_: InvalidProtocolBufferException) {
            throw DataFormatException("Protobuf couldn't deserialize packet")
        }

        if (proto.serviceName != SERVICE_NAME) {
            throw UnknownSourceException()
        }

        return when (proto.type) {
            TYPE_MESSAGE -> Packet.Message(
                senderId = proto.senderUuid,
                senderUsername = proto.senderUsername,
                message = proto.payload,
                timeStamp = proto.timestamp
            )

            TYPE_TYPING -> Packet.Typing(
                senderId = proto.senderUuid
            )

            TYPE_HELLO -> Packet.Hello(
                senderId = proto.senderUuid,
                senderUsername = proto.senderUsername
            )

            TYPE_HEARTBEAT -> Packet.Heartbeat(
                senderId = proto.senderUuid,
                senderUsername = proto.senderUsername
            )

            TYPE_GOODBYE -> Packet.GoodBye(
                senderId = proto.senderUuid,
            )

            else -> throw IllegalArgumentException("Unknown packet type: ${proto.type}")
        }
    }

    private const val TYPE_MESSAGE = "message"
    private const val TYPE_TYPING = "typing"
    private const val TYPE_HELLO = "hello"
    private const val TYPE_HEARTBEAT = "heartbeat"
    private const val TYPE_GOODBYE = "goodbye"
    private const val SERVICE_NAME = "REACH_SERVICE"
}