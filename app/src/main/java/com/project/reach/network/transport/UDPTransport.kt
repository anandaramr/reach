package com.project.reach.network.transport

import android.util.Log
import com.project.reach.util.debug
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class UDPTransport: NetworkTransport {

    private val socket by lazy {
        DatagramSocket(NetworkTransport.PORT).apply {
            broadcast = true
        }
    }
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverJob: Job? = null

    override fun listen(handleClient: suspend (clientIp: InetAddress, bytes: ByteArray) -> Unit) {
        if (serverJob != null) return

        serverJob = scope.launch {
            debug("Listening on ${socket.localAddress}:${socket.localPort}")
            while (isActive) {
                val buffer = ByteArray(1024)
                val packet = DatagramPacket(buffer, buffer.size)

                try {
                    socket.receive(packet)
                    launch {
                        handleClient(packet.address, packet.data.copyOf(packet.length))
                    }
                } catch (e: SocketException) {
                    if (!isActive) break
                }
            }
        }
    }

    override suspend fun send(bytes: ByteArray, ip: InetAddress): Boolean {
        val packet = DatagramPacket(bytes, bytes.size, ip, NetworkTransport.PORT)

        return try {
            socket.send(packet)
            true
        } catch (e: Exception) {
            debug("Error: $e")
            false
        }
    }

    override fun close() {
        Log.d("DBG", "[UDP] Closing...")
        runCatching { serverJob?.cancel() }
        socket.close()
    }
}