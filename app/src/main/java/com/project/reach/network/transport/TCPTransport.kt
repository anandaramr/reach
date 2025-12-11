package com.project.reach.network.transport

import com.project.reach.network.model.NetworkPacket
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.EOFException
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

class TCPTransport: NetworkTransport {
    private var socket: ServerSocket? = null

    private val _incomingPackets = MutableSharedFlow<NetworkPacket>(
        replay = 0,
        extraBufferCapacity = 64,
        onBufferOverflow = BufferOverflow.SUSPEND
    )
    override val incomingPackets = _incomingPackets.asSharedFlow()

    private val connections = ConcurrentHashMap<InetAddress, Socket>()

    val supervisorJob = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + supervisorJob)
    private var serverJob: Job? = null

    private fun listen() {
        if (serverJob != null) return
        socket?.let { socket ->
            debug("[TCP] listening on ${socket.inetAddress}:${socket.localPort}")
            serverJob = scope.launch {
                while (isActive) {
                    try {
                        val clientSocket = socket.accept()
                        debug("Accepted connection from ${clientSocket.inetAddress}")
                        launch {
                            handlePeerSocket(clientSocket)
                        }
                    } catch (_: SocketException) {
                        if (!isActive) break
                    } catch (e: Exception) {
                        debug("TCP server socket error: $e")
                    }
                }
            }
        }
    }

    private suspend fun handlePeerSocket(
        clientSocket: Socket
    ) = coroutineScope {
        val clientIp = clientSocket.inetAddress
        connections.putIfAbsent(clientIp, clientSocket)

        clientSocket.soTimeout = IDLE_TIMEOUT
        val input = DataInputStream(clientSocket.inputStream)

        try {
            while (isActive && !clientSocket.isClosed) {
                try {
                    handleIncomingBytes(clientIp, input)
                } catch (_: SocketTimeoutException) {
                    debug("Socket timeout: $clientIp")
                    break
                } catch (_: EOFException) {
                    debug("Client disconnected: $clientIp")
                    break
                }
            }
        } catch (e: Exception) {
            debug("TCP Error handling client: $e")
        } finally {
            debug("Disconnected from $clientIp")
            connections.remove(clientIp, clientSocket)
            clientSocket.close()
        }
    }

    private suspend fun handleIncomingBytes(
        clientIp: InetAddress,
        input: DataInputStream
    ) {
        val messageLength = input.readInt()
        val messageBuffer = ByteArray(messageLength)
        input.readFully(messageBuffer)

        _incomingPackets.emit(
            NetworkPacket(
                address = clientIp,
                payload = messageBuffer
            )
        )
    }

    override suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean {
        try {
            val socket = getOrCreateSocket(ip)
            val output = DataOutputStream(socket.outputStream)
            output.writeInt(bytes.size)
            output.write(bytes)
            output.flush()
            return true
        } catch (e: Exception) {
            debug("TCP error: couldn't send(): $e")
            return false
        }
    }

    /**
     * Retrieves socket of client if already connected, creates
     * a new one otherwise
     */
    private fun getOrCreateSocket(ip: InetAddress): Socket {
        return connections.computeIfAbsent(ip) { key ->
            Socket(key, NetworkTransport.PORT).also { socket ->
                debug("connected to ${socket.inetAddress}")
                scope.launch {
                    handlePeerSocket(socket)
                }
            }
        }
    }

    override fun start() {
        debug("[TCP] starting")
        if (socket != null) return
        socket = ServerSocket(NetworkTransport.PORT)
        listen()
    }

    override fun close() {
        runCatching { serverJob?.cancel() }
        socket?.close()
        socket = null
        serverJob = null
        connections.values.forEach { runCatching { it.close() } }
        connections.clear()
    }

    companion object {
        private const val IDLE_TIMEOUT = 60_000
    }
}