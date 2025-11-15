package com.project.reach.network.transport

import com.project.reach.util.debug
import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketTimeoutException
import kotlin.math.min

class DataInputChannel(
    private val senderIp: InetAddress,
): Closeable {
    private val socket = ServerSocket(0).apply { soTimeout = WAIT_TIMEOUT }
    val port = socket.localPort

    fun readInto(output: OutputStream, size: Long): Boolean {
        try {
            while (true) {
                val client = socket.accept()

                try {
                    val input = client.inputStream
                    if (client.inetAddress == senderIp) {
                        debug("Receiving $size bytes from $senderIp")
                        handleClient(input, output, size)
                        return true
                    }
                } catch (e: Exception) {
                    debug("Error receiving data: $e")
                    return false
                } finally {
                    client.close()
                }
            }
        } catch (_: SocketTimeoutException) {
            debug("Data receive timeout: $senderIp")
            return false
        }
    }

    private fun handleClient(input: InputStream, output: OutputStream, size: Long) {
        val buffer = ByteArray(8192)
        var totalRead = 0L

        while (totalRead < size) {
            val bytesToRead = min(buffer.size.toLong(), size - totalRead).toInt()
            val receivedBytes = input.read(buffer, 0, bytesToRead)
            if (receivedBytes <= 0) {
                throw IOException("Unexpected end of file")
            }
            output.write(buffer, 0, receivedBytes)
            totalRead += receivedBytes
        }
    }

    override fun close() {
        socket.close()
    }

    private companion object {
        private const val WAIT_TIMEOUT = 30_000
    }
}

class DataOutputChannel(
    private val receiverIp: InetAddress,
    private val port: Int
): Closeable {
    private val socket = Socket().apply { soTimeout = WAIT_TIMEOUT }

    fun writeFrom(input: InputStream, size: Long): Boolean {
        val output = try {
            socket.connect(InetSocketAddress(receiverIp, port), WAIT_TIMEOUT)
            socket.outputStream
        } catch (_: Exception) {
            debug("Error connecting to $receiverIp:$port on ${this::class.simpleName}")
            return false
        }

        val buffer = ByteArray(8192)
        var totalBytesSent = 0L
        try {
            while (totalBytesSent < size) {
                val bytesToRead = min(buffer.size.toLong(), size - totalBytesSent).toInt()
                val readBytes = input.read(buffer, 0, bytesToRead)
                if (readBytes <= 0) throw IOException("Unexpected end of file")
                output.write(buffer, 0, bytesToRead)
                totalBytesSent += readBytes
            }
            return true
        } catch (e: Exception) {
            debug("Error sending data: $e")
            return false
        }
    }

    override fun close() {
        socket.close()
    }

    private companion object {
        private const val WAIT_TIMEOUT = 30_000
    }
}