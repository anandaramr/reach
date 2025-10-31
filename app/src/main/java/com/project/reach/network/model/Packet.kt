package com.project.reach.network.model

import com.google.protobuf.InvalidProtocolBufferException
import com.project.reach.core.exceptions.UnknownSourceException
import com.reach.project.core.serialization.ReachPacket
import java.util.zip.DataFormatException

sealed class Packet {
    data class Message(
        override val userId: String,
        val username: String,
        val message: String,
        val timeStamp: Long = System.currentTimeMillis()
    ): Packet()

    data class Typing(
        override val userId: String
    ): Packet()

    data class Hello(
        override val userId: String,
        val username: String
    ): Packet()

    data class Heartbeat(
        override val userId: String,
        val username: String
    ): Packet()

    data class GoodBye(
        override val userId: String
    ): Packet()

    abstract val userId: String

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
         * @throws UnknownSourceException if the received packet is from an unkown service
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
                        senderUuid = packet.userId
                        senderUsername = packet.username
                        payload = packet.message
                        timestamp = packet.timeStamp
                    }
                    is Packet.Typing -> {
                        type = TYPE_TYPING
                        senderUuid = packet.userId
                    }
                    is Packet.Heartbeat -> {
                        type = TYPE_HEARTBEAT
                        senderUuid = packet.userId
                        senderUsername = packet.username
                    }
                    is Packet.Hello -> {
                        type = TYPE_HELLO
                        senderUuid = packet.userId
                        senderUsername = packet.username
                    }
                    is Packet.GoodBye -> {
                        type = TYPE_GOODBYE
                        senderUuid = packet.userId
                    }
                }
            }
        return builder.build().toByteArray()
    }

    fun deserialize(bytes: ByteArray): Packet {
        val proto = try {
            ReachPacket.parseFrom(bytes)
        } catch (e: InvalidProtocolBufferException) {
            throw DataFormatException("Protobuf couldn't deserialize packet")
        }

        if (proto.serviceName != SERVICE_NAME) {
            throw UnknownSourceException()
        }

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

            TYPE_HELLO -> Packet.Hello(
                userId = proto.senderUuid,
                username = proto.senderUsername
            )

            TYPE_HEARTBEAT -> Packet.Heartbeat(
                userId = proto.senderUuid,
                username = proto.senderUsername
            )

            TYPE_GOODBYE -> Packet.GoodBye(
                userId = proto.senderUuid,
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