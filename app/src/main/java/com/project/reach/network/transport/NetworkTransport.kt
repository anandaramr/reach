package com.project.reach.network.transport

interface NetworkTransport {
    fun listen(handleClient: suspend (message: ByteArray) -> Unit)
    fun close()
}