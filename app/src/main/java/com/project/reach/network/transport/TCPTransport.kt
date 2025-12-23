package com.project.reach.network.transport

import android.net.Network
import android.os.ParcelFileDescriptor
import android.system.Os
import android.system.OsConstants
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
    private var currentNetwork: Network? = null

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
        if (serverJob != null) {
            debug("server already started")
            return
        }

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
        val oldSocket = connections.put(clientIp, clientSocket)
        oldSocket?.close()

        try {
            val input = DataInputStream(clientSocket.inputStream)
            clientSocket.soTimeout = IDLE_TIMEOUT
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
            e.printStackTrace()
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
            currentNetwork?.let { network ->
                val socket = getOrCreateSocket(ip, network)
                val output = DataOutputStream(socket.outputStream)
                output.writeInt(bytes.size)
                output.write(bytes)
                output.flush()
                return true
            }
            return false
        } catch (e: Exception) {
            debug("TCP error: couldn't send(): $e")
            e.printStackTrace()
            return false
        }
    }

    /**
     * Retrieves socket of client if already connected, creates
     * a new one otherwise
     */
    private fun getOrCreateSocket(ip: InetAddress, network: Network): Socket {
        connections[ip]?.let { if (!it.isClosed) return it }
        val clientSocket = network.socketFactory.createSocket(ip, NetworkTransport.PORT)
        debug("Connected to ${clientSocket.inetAddress.hostAddress}")
        scope.launch {
            handlePeerSocket(clientSocket)
        }
        return clientSocket
    }

    override fun start(hostAddress: InetAddress, network: Network) {
        debug("[TCP] starting")
        if (socket != null) {
            debug("[TCP] already started")
            return
        }

        socket = ServerSocket(
            NetworkTransport.PORT,
            NetworkTransport.BACKLOG,
            hostAddress
        )
        currentNetwork = network
        listen()
    }

    override fun close() {
        debug("[TCP] closing")
        runCatching { serverJob?.cancel() }
        socket?.close()

        socket = null
        serverJob = null
        currentNetwork = null

        connections.values.forEach { runCatching { it.close() } }
        connections.clear()
    }

    private companion object {
        const val IDLE_TIMEOUT = 60_000
        const val LINUX_TCP_USER_TIMEOUT = 18
        const val TCP_WRITE_DELAY = 3000
    }
}