package com.project.reach.network.transport

import com.project.reach.network.model.NetworkPacket
import kotlinx.coroutines.flow.SharedFlow
import java.net.InetAddress

interface NetworkTransport {
    val incomingPackets: SharedFlow<NetworkPacket>

    suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean

    fun start()

    fun close()

    companion object {
        const val PORT = 5000
    }
}