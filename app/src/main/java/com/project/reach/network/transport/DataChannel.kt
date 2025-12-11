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

object DataChannelConfig {
    const val CHUNK_SIZE = 8192
    const val WAIT_TIMEOUT = 10_000
    const val THROTTLE_TIME = 1000
}

class DataInputChannel(
    private val peerIp: InetAddress,
): Closeable {
    private val socket = ServerSocket(0).apply { soTimeout = DataChannelConfig.WAIT_TIMEOUT }
    val port = socket.localPort

    suspend fun readInto(output: OutputStream, expectedBytes: Long, onProgress: (Long) -> Unit): Boolean {
        if (socket.isClosed) {
            debug("${this::class.simpleName}: Use after close")
            return false
        }

        try {
            while (true) {
                socket.accept().use { client ->
                    if (client.inetAddress == peerIp) {
                        debug("Receiving $expectedBytes bytes from $peerIp")
                        val input = client.inputStream
                        handleClient(input, output, expectedBytes, onProgress)
                        return true
                    }
                }
            }
        } catch (_: SocketTimeoutException) {
            debug("Data receive timeout: $peerIp")
            return false
        } catch (e: Exception) {
            debug("Error while receiving file: $e")
            e.printStackTrace()
            return false
        }
    }

    private suspend fun handleClient(
        input: InputStream,
        output: OutputStream,
        expectedBytes: Long,
        onProgress: (Long) -> Unit
    ) =
        withContext(Dispatchers.IO) {
            runInterruptible {
                onProgress(0)

                val buffer = ByteArray(DataChannelConfig.CHUNK_SIZE)
                var totalRead = 0L
                var lastProgressUpdate = 0L

                // TODO check transfer fails
                while (totalRead < expectedBytes) {
                    val bytesToRead = min(buffer.size.toLong(), expectedBytes - totalRead).toInt()
                    val receivedBytes = input.read(buffer, 0, bytesToRead)
                    if (receivedBytes <= 0) {
                        throw IOException("Unexpected end of file")
                    }

                    output.write(buffer, 0, receivedBytes)
                    totalRead += receivedBytes

                    val time = System.currentTimeMillis()
                    if (time - lastProgressUpdate >= DataChannelConfig.THROTTLE_TIME) {
                        onProgress(totalRead)
                        lastProgressUpdate = time
                    }
                }

                output.flush()
            }
        }

    override fun close() {
        socket.close()
    }
}

class DataOutputChannel(
    private val peerIp: InetAddress,
    private val port: Int
): Closeable {
    private val socket = Socket().apply { soTimeout = DataChannelConfig.WAIT_TIMEOUT }

    fun writeFrom(input: InputStream, bytesToSend: Long, onProgress: (Long) -> Unit): Boolean {
        if (socket.isClosed) {
            debug("${this::class.simpleName}: Use after close")
            return false
        }

        val output = try {
            socket.connect(InetSocketAddress(peerIp, port), DataChannelConfig.WAIT_TIMEOUT)
            socket.outputStream
        } catch (_: Exception) {
            debug("Error connecting to $peerIp:$port on ${this::class.simpleName}")
            return false
        }

        onProgress(0)
        val buffer = ByteArray(DataChannelConfig.CHUNK_SIZE)
        var totalBytesSent = 0L
        var lastProgressUpdate = 0L
        try {
            while (totalBytesSent < bytesToSend) {
                val bytesToRead = min(buffer.size.toLong(), bytesToSend - totalBytesSent).toInt()
                val readBytes = input.read(buffer, 0, bytesToRead)
                if (readBytes <= 0) {
                    throw IOException("Unexpected end of file")
                }

                output.write(buffer, 0, bytesToRead)
                totalBytesSent += readBytes

                val time = System.currentTimeMillis()
                if (time - lastProgressUpdate >= DataChannelConfig.THROTTLE_TIME) {
                    onProgress(totalBytesSent)
                    lastProgressUpdate = time
                }
            }
            return true
        } catch (e: Exception) {
            debug("Error sending data: $e")
            e.printStackTrace()
            return false
        }
    }

    override fun close() {
        socket.close()
    }
}