package com.project.reach.network.transport

import com.project.reach.network.model.NetworkPacket
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class UDPTransport(
): NetworkTransport {
    private var socket: DatagramSocket? = null
    private val socketLock = Mutex()

    private val _incomingPackets =
        MutableSharedFlow<NetworkPacket>(extraBufferCapacity = 64, replay = 0)
    override val incomingPackets = _incomingPackets.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverJob: Job? = null

    private fun listen() {
        serverJob = scope.launch {
            debug("[UDP] Listening on ${socket?.localAddress}:${socket?.localPort}")
            while (isActive && socket?.isClosed == false) {
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                try {
                    socket?.receive(packet)

                    _incomingPackets.emit(NetworkPacket(
                        address = packet.address,
                        payload = packet.data.copyOf(packet.length)
                    ) )
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
        if (socketLock.isLocked) {
            debug("Failed to send: UDP socket is in locked state")
            return false
        }

        val packet = DatagramPacket(bytes, bytes.size, ip, NetworkTransport.PORT)
        return socketLock.withLock {
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
        if (socket != null) return
        socket = DatagramSocket(NetworkTransport.PORT).apply {
            broadcast = true
        }
        listen()
    }

    override fun close() {
        debug("[UDP] Closing")
        runCatching { serverJob?.cancel() }
        serverJob = null
        socket?.close()
        socket = null
    }
}