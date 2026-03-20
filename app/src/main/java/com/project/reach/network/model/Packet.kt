package com.project.reach.network.model

sealed class Packet {
    data class Message(
        override val senderId: String,
        val messageId: String,
        val senderUsername: String,
        val text: String,
        val timeStamp: Long,
        val media: FileMetadata?
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

    data class FileMetadata(
        val fileHash: String,
        val filename: String,
        val mimeType: String,
        val fileSize: Long
    )

    data class FileAccept(
        override val senderId: String,
        val fileHash: String,
        val port: Int,
        val offset: Long
    ): Packet()

    data class FileComplete(
        override val senderId: String,
        val fileHash: String
    ): Packet()

    abstract val senderId: String

    sealed class CallSignal: Packet() {
        abstract val callId: String

        data class CallInit(
            override val callId: String,
            override val senderId: String,
            val senderUsername: String,
            val offerSdp: String
        ): CallSignal()

        data class CallAccept(
            override val callId: String,
            override val senderId: String,
            val answerSdp: String
        ): CallSignal()

        data class CallDecline(
            override val callId: String,
            override val senderId: String
        ): CallSignal()

        data class CallCancel(
            override val callId: String,
            override val senderId: String
        ): CallSignal()

        data class CallEnd(
            override val callId: String,
            override val senderId: String
        ): CallSignal()

        data class IceCandidate(
            override val callId: String,
            override val senderId: String,
            val candidate: String,
            val sdpMid: String,
            val mLineIndex: Int
        ): CallSignal()
    }

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