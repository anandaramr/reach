package com.project.reach.network.transport

import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class UDPTransport: NetworkTransport {

    private var socket: DatagramSocket? = null

    private val sendLock = Mutex()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverJob: Job? = null

    override fun listen(handleClient: suspend (clientIp: InetAddress, bytes: ByteArray) -> Unit) {
        if (serverJob != null) return
        val currentSocket = socket
        if (currentSocket == null) {
            debug("ERROR: UDP Transport hasn't started")
            return
        }

        serverJob = scope.launch {
            debug("Listening on ${currentSocket.localAddress}:${currentSocket.localPort}")
            while (isActive) {
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                try {
                    currentSocket.receive(packet)
                    launch {
                        handleClient(packet.address, packet.data.copyOf(packet.length))
                    }
                } catch (_: SocketException) {
                    if (!isActive) break
                }
            }
        }
    }

    override suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean {
        val currentSocket = socket
        if (currentSocket == null) {
            debug("ERROR: UDP Transport hasn't started")
            return false
        }
        if (sendLock.isLocked) {
            debug("Failed to send: UDP socket is in locked state")
            return false
        }

        val packet = DatagramPacket(bytes, bytes.size, ip, NetworkTransport.PORT)
        return sendLock.withLock {
            try {
                currentSocket.send(packet)
                true
            } catch (e: Exception) {
                debug("Error: $e")
                false
            }
        }
    }

    override fun start() {
        debug("[UDP] Starting")
        socket = DatagramSocket(NetworkTransport.PORT).apply {
            broadcast = true
        }
    }

    override fun close() {
        debug("[UDP] Closing")
        runCatching { serverJob?.cancel() }
        serverJob = null
        socket?.close()
    }
}