package com.project.reach.network.model

import com.google.protobuf.InvalidProtocolBufferException
import com.project.reach.util.toHexString
import com.project.reach.util.toProtoBytes
import com.reach.project.core.serialization.CallAccept
import com.reach.project.core.serialization.CallCancel
import com.reach.project.core.serialization.CallDecline
import com.reach.project.core.serialization.CallEnd
import com.reach.project.core.serialization.CallInit
import com.reach.project.core.serialization.CallSignal
import com.reach.project.core.serialization.FileAccept
import com.reach.project.core.serialization.FileComplete
import com.reach.project.core.serialization.FileHeader
import com.reach.project.core.serialization.Goodbye
import com.reach.project.core.serialization.Heartbeat
import com.reach.project.core.serialization.Hello
import com.reach.project.core.serialization.IceCandidate
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
                                    fileHash = media.fileHash.toProtoBytes()
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

                    is Packet.CallSignal.CallAccept -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            callAccept = CallAccept.newBuilder().apply {
                                answerSdp = packet.answerSdp
                            }.build()
                        }.build()
                    }

                    is Packet.CallSignal.CallCancel -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            callCancel = CallCancel.newBuilder().build()
                        }.build()
                    }

                    is Packet.CallSignal.CallDecline -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            callDecline = CallDecline.newBuilder().build()
                        }.build()
                    }

                    is Packet.CallSignal.CallEnd -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            callEnd = CallEnd.newBuilder().build()
                        }.build()
                    }

                    is Packet.CallSignal.CallInit -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            callInit = CallInit.newBuilder().apply {
                                senderUsername = packet.senderUsername
                                offerSdp = packet.offerSdp
                            }.build()
                        }.build()
                    }

                    is Packet.CallSignal.IceCandidate -> {
                        callSignal = CallSignal.newBuilder().apply {
                            callId = packet.callId
                            iceCandidate = IceCandidate.newBuilder().apply {
                                candidate = packet.candidate
                                sdpMid = packet.sdpMid
                                mLineIndex = packet.mLineIndex
                            }.build()
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
                Packet.Message(
                    senderId = senderId,
                    senderUsername = proto.message.senderUsername,
                    messageId = proto.message.messageId,
                    text = proto.message.text,
                    timeStamp = proto.message.timestamp,
                    media = if (proto.message.hasFileHeader()) {
                        val header = proto.message.fileHeader
                        Packet.FileMetadata(
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
                    fileHash = proto.fileAccept.fileHash.toHexString(),
                    port = proto.fileAccept.port,
                    offset = proto.fileAccept.offset
                )
            }

            ReachPacket.PayloadCase.FILE_COMPLETE -> {
                Packet.FileComplete(
                    senderId = senderId,
                    fileHash = proto.fileComplete.fileHash.toHexString()
                )
            }

            ReachPacket.PayloadCase.CALL_SIGNAL -> {
                val callId = proto.callSignal.callId

                when (proto.callSignal.payloadCase) {
                    CallSignal.PayloadCase.CALL_ACCEPT -> {
                        Packet.CallSignal.CallAccept(
                            callId = callId,
                            senderId = senderId,
                            answerSdp = proto.callSignal.callAccept.answerSdp,
                        )
                    }

                    CallSignal.PayloadCase.CALL_CANCEL -> {
                        Packet.CallSignal.CallCancel(
                            callId = callId,
                            senderId = senderId,
                        )
                    }

                    CallSignal.PayloadCase.CALL_DECLINE -> {
                        Packet.CallSignal.CallDecline(
                            callId = callId,
                            senderId = senderId,
                        )
                    }

                    CallSignal.PayloadCase.CALL_END -> {
                        Packet.CallSignal.CallEnd(
                            callId = callId,
                            senderId = senderId,
                        )
                    }

                    CallSignal.PayloadCase.CALL_INIT -> {
                        Packet.CallSignal.CallInit(
                            callId = callId,
                            senderId = senderId,
                            senderUsername = proto.callSignal.callInit.senderUsername,
                            offerSdp = proto.callSignal.callInit.offerSdp,
                        )
                    }

                    CallSignal.PayloadCase.ICE_CANDIDATE -> {
                        Packet.CallSignal.IceCandidate(
                            callId = callId,
                            senderId = senderId,
                            candidate = proto.callSignal.iceCandidate.candidate,
                            sdpMid = proto.callSignal.iceCandidate.sdpMid,
                            mLineIndex = proto.callSignal.iceCandidate.mLineIndex,
                        )
                    }

                    CallSignal.PayloadCase.PAYLOAD_NOT_SET -> {
                        throw IllegalArgumentException("CallSignal: Payload not set")
                    }
                }
            }
        }
    }

    private const val SERVICE_NAME = "REACH_SERVICE"
}