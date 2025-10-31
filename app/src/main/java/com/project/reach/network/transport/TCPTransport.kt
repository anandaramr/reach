package com.project.reach.network.transport

import com.project.reach.network.model.NetworkPacket
import com.project.reach.util.debug
import com.project.reach.util.toByteArray
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.InputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import java.net.SocketTimeoutException
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentHashMap

class TCPTransport: NetworkTransport {
    private var socket: ServerSocket? = null

    private val _incomingPackets = MutableSharedFlow<NetworkPacket>()
    override val incomingPackets = _incomingPackets.asSharedFlow()

    private val connections = ConcurrentHashMap<InetAddress, Socket>()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var serverJob: Job? = null

    private fun listen() {
        if (serverJob != null) return
        socket?.let { socket ->
            serverJob = scope.launch {
                while (isActive) {
                    try {
                        val clientSocket = socket.accept()
                        debug("connected to ${clientSocket.inetAddress}")
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
        connections.put(clientIp, clientSocket)

        var lastSeen = getCurrentTime()
        clientSocket.soTimeout = SOCK_TIMEOUT
        val input = clientSocket.inputStream

        try {
            while (isActive && !clientSocket.isClosed) {
                try {
                    val isConnectionAlive = handleIncomingBytes(clientIp, input)
                    if (!isConnectionAlive) break
                    lastSeen = getCurrentTime()
                } catch (_: SocketTimeoutException) {
                    if (getCurrentTime() - lastSeen > IDLE_TIMEOUT) {
                        break
                    }
                }
            }
        } catch (e: Exception) {
            debug("TCP Error handling client: $e")
        } finally {
            connections.remove(clientIp)
            clientSocket.close()
        }
    }

    private suspend fun handleIncomingBytes(clientIp: InetAddress, input: InputStream): Boolean {
        val messageLength = ByteArray(4)
        val bytes = input.read(messageLength)
        if (bytes == -1) return false

        val messageSize = ByteBuffer.wrap(messageLength).int
        val messageBuffer = ByteArray(messageSize)
        var bytesRead = 0
        while (bytesRead < messageSize) {
            val read = input.read(messageBuffer, bytesRead, messageSize - bytesRead)
            if (read == -1) return false
            bytesRead += read
        }

        _incomingPackets.emit(NetworkPacket(
            address = clientIp,
            payload = messageBuffer.copyOf(bytesRead)
        ))
        return true
    }

    override suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean {
        try {
            val socket = getOrCreateSocket(ip)
            val output = socket.outputStream

            // write output size first and then data
            val size = bytes.size.toByteArray()
            output.write(size)
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
        val savedSocket = connections[ip]
        if (savedSocket != null) return savedSocket

        return Socket(ip, NetworkTransport.PORT).also { socket ->
            val socket = Socket(ip, NetworkTransport.PORT)
            debug("connected to ${socket.inetAddress}")
            connections.put(ip, socket)

            scope.launch {
                handlePeerSocket(socket)
            }
        }
    }

    override fun start() {
        if (socket != null) return
        socket = ServerSocket(NetworkTransport.PORT)
        listen()
    }

    override fun close() {
        runCatching{ serverJob?.cancel() }
        socket?.close()
        socket = null
    }

    private fun getCurrentTime() = System.currentTimeMillis()

    companion object {
        private const val SOCK_TIMEOUT = 10_000
        private const val IDLE_TIMEOUT = 60_000
    }
}