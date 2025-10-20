package com.project.reach.network.transport

import java.net.InetAddress

interface NetworkTransport {
    fun listen(handleClient: suspend (message: ByteArray) -> Unit)

    suspend fun send(bytes: ByteArray, ip: InetAddress, port: Int): Boolean

    fun close()
}