package com.project.reach.network.model

import com.google.protobuf.InvalidProtocolBufferException
import com.project.reach.core.exceptions.UnknownSourceException
import com.project.reach.network.model.Packet.*
import com.project.reach.util.toHexString
import com.project.reach.util.toProtoBytes
import com.reach.project.core.serialization.FileAccept
import com.reach.project.core.serialization.FileComplete
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
                    is GoodBye -> {
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
                                    fileHash = media.fileHash.toProtoBytes()
                                    filename = media.filename
                                    fileSize = media.fileSize
                                    mimeType = media.mimeType
                                }.build()
                            }
                        }.build()
                    }

                    is Typing -> {
                        typingIndicator = TypingIndicator.newBuilder().build()
                    }

                    is Packet.FileAccept -> {
                        fileAccept = FileAccept.newBuilder().apply {
                            fileHash = packet.fileHash.toProtoBytes()
                            port = packet.port
                            offset = packet.offset
                        }.build()
                    }

                    is Packet.FileComplete -> {
                        fileComplete = FileComplete.newBuilder().apply {
                            fileHash = packet.fileHash.toProtoBytes()
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
            throw IllegalArgumentException("Got packet from an unknown source: ${proto.serviceName}")
        }

        val senderId = proto.senderId
        return when (proto.payloadCase) {
            ReachPacket.PayloadCase.MESSAGE -> {
                Message(
                    senderId = senderId,
                    senderUsername = proto.message.senderUsername,
                    messageId = proto.message.messageId,
                    text = proto.message.text,
                    timeStamp = proto.message.timestamp,
                    media = if (proto.message.hasFileHeader()) {
                        val header = proto.message.fileHeader
                        FileMetadata(
                            fileHash = header.fileHash.toHexString(),
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
                Typing(senderId = senderId)
            }

            ReachPacket.PayloadCase.HELLO -> {
                Hello(
                    senderId = senderId,
                    senderUsername = proto.hello.senderUsername
                )
            }

            ReachPacket.PayloadCase.HEARTBEAT -> {
                Heartbeat(
                    senderId = senderId,
                    senderUsername = proto.heartbeat.senderUsername
                )
            }

            ReachPacket.PayloadCase.GOODBYE -> {
                GoodBye(
                    senderId = senderId
                )
            }

            ReachPacket.PayloadCase.PAYLOAD_NOT_SET -> {
                throw IllegalArgumentException("Payload not set")
            }

            ReachPacket.PayloadCase.FILE_ACCEPT -> {
                FileAccept(
                    senderId = senderId,
                    fileHash = proto.fileAccept.fileHash.toHexString(),
                    port = proto.fileAccept.port,
                    offset = proto.fileAccept.offset
                )
            }

            ReachPacket.PayloadCase.FILE_COMPLETE -> {
                FileComplete(
                    senderId = senderId,
                    fileHash = proto.fileComplete.fileHash.toHexString()
                )
            }
        }
    }

    private const val SERVICE_NAME = "REACH_SERVICE"
}