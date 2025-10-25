package com.project.reach.network.transport

import java.net.InetAddress

interface NetworkTransport {
    fun listen(handleClient: suspend (ip: InetAddress, message: ByteArray) -> Unit)

    suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean

    fun close()

    companion object {
        const val PORT = 5000
    }
}