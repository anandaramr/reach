package com.project.reach.network.transport

import com.project.reach.util.debug
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
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
    private val peerIp: InetAddress,
): Closeable {
    private val socket = ServerSocket(0).apply { soTimeout = WAIT_TIMEOUT }
    val port = socket.localPort

    suspend fun readInto(output: OutputStream, size: Long): Boolean {
        if (socket.isClosed) {
            debug("${this::class.simpleName}: Use after close")
            return false
        }

        try {
            val client = socket.accept()
            val input = client.inputStream
            debug("Receiving $size bytes from $peerIp")
            // TODO check if sender is valid

            handleClient(input, output, size)
            client.close()
            return true
        } catch (_: SocketTimeoutException) {
            debug("Data receive timeout: $peerIp")
            return false
        }
    }

    private suspend fun handleClient(input: InputStream, output: OutputStream, size: Long) =
        withContext(Dispatchers.IO) {
            runInterruptible {
                val buffer = ByteArray(8192)
                var totalRead = 0L

                // TODO check transfer fails
                while (totalRead < size) {
                    val bytesToRead = min(buffer.size.toLong(), size - totalRead).toInt()
                    val receivedBytes = input.read(buffer, 0, bytesToRead)
                    if (receivedBytes <= 0) {
                        throw IOException("Unexpected end of file")
                    }
                    output.write(buffer, 0, receivedBytes)
                    totalRead += receivedBytes
                }

                output.flush()
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
    private val peerIp: InetAddress,
    private val port: Int
): Closeable {
    private val socket = Socket().apply { soTimeout = WAIT_TIMEOUT }

    fun writeFrom(input: InputStream, size: Long): Boolean {
        if (socket.isClosed) {
            debug("${this::class.simpleName}: Use after close")
            return false
        }

        val output = try {
            socket.connect(InetSocketAddress(peerIp, port), WAIT_TIMEOUT)
            socket.outputStream
        } catch (_: Exception) {
            debug("Error connecting to $peerIp:$port on ${this::class.simpleName}")
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