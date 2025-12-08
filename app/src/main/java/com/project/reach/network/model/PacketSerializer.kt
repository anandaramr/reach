package com.project.reach.network.model

import com.google.protobuf.InvalidProtocolBufferException
import com.project.reach.core.exceptions.UnknownSourceException
import com.reach.project.core.serialization.FileAccept
import com.reach.project.core.serialization.FileHeader
import com.reach.project.core.serialization.Goodbye
import com.reach.project.core.serialization.Heartbeat
import com.reach.project.core.serialization.Hello
import com.reach.project.core.serialization.Message
import com.reach.project.core.serialization.ReachPacket
import com.reach.project.core.serialization.TypingIndicator

internal object PacketSerializer {
    fun serialize(packet: Packet): ByteArray {
        val builder = ReachPacket
            .newBuilder()
            .setServiceName(SERVICE_NAME)
            .apply {
                senderId = packet.senderId

                when (packet) {
                    is Packet.GoodBye -> {
                        goodbye = Goodbye.newBuilder().build()
                    }

                    is Packet.Heartbeat -> {
                        heartbeat = Heartbeat.newBuilder().apply {
                            senderUsername = packet.senderUsername
                        }.build()
                    }

                    is Packet.Hello -> {
                        hello = Hello.newBuilder().apply {
                            senderUsername = packet.senderUsername
                        }.build()
                    }

                    is Packet.Message -> {
                        message = Message.newBuilder().apply {
                            senderUsername = packet.senderUsername
                            messageId = packet.messageId
                            text = packet.text
                            timestamp = packet.timeStamp

                            packet.media?.let { media ->
                                fileHeader = FileHeader.newBuilder().apply {
                                    fileId = media.fileId
                                    filename = media.filename
                                    fileSize = media.fileSize
                                    mimeType = media.mimeType
                                }.build()
                            }
                        }.build()
                    }

                    is Packet.Typing -> {
                        typingIndicator = TypingIndicator.newBuilder().build()
                    }

                    is Packet.FileAccept -> {
                        fileAccept = FileAccept.newBuilder().apply {
                            fileId = packet.fileId
                            port = packet.port
                        }.build()
                    }
                }
            }
        return builder.build().toByteArray()
    }

    fun deserialize(bytes: ByteArray): Packet {
        val proto = try {
            ReachPacket.parseFrom(bytes)
        } catch (_: InvalidProtocolBufferException) {
            throw IllegalArgumentException("Protobuf couldn't deserialize packet")
        }

        if (proto.serviceName != SERVICE_NAME) {
            throw UnknownSourceException()
        }

        val senderId = proto.senderId
        return when (proto.payloadCase) {
            ReachPacket.PayloadCase.MESSAGE -> {
                Packet.Message(
                    senderId = senderId,
                    senderUsername = proto.message.senderUsername,
                    messageId = proto.message.messageId,
                    text = proto.message.text,
                    timeStamp = proto.message.timestamp,
                    media = if (proto.message.hasFileHeader()) {
                        val header = proto.message.fileHeader
                        Packet.FileMetadata(
                            fileId = header.fileId,
                            filename = header.filename,
                            mimeType = header.mimeType,
                            fileSize = header.fileSize
                        )
                    } else {
                        null
                    }
                )
            }

            ReachPacket.PayloadCase.TYPING_INDICATOR -> {
                Packet.Typing(senderId = senderId)
            }

            ReachPacket.PayloadCase.HELLO -> {
                Packet.Hello(
                    senderId = senderId,
                    senderUsername = proto.hello.senderUsername
                )
            }

            ReachPacket.PayloadCase.HEARTBEAT -> {
                Packet.Heartbeat(
                    senderId = senderId,
                    senderUsername = proto.heartbeat.senderUsername
                )
            }

            ReachPacket.PayloadCase.GOODBYE -> {
                Packet.GoodBye(
                    senderId = senderId
                )
            }

            ReachPacket.PayloadCase.PAYLOAD_NOT_SET -> {
                throw IllegalArgumentException("Payload not set")
            }

            ReachPacket.PayloadCase.FILE_ACCEPT -> {
                Packet.FileAccept(
                    senderId = senderId,
                    fileId = proto.fileAccept.fileId,
                    port = proto.fileAccept.port
                )
            }
        }
    }

    private const val SERVICE_NAME = "REACH_SERVICE"
}