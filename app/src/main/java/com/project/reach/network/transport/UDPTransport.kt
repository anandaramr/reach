package com.project.reach.network.transport

import android.util.Log

class UDPTransport: NetworkTransport {
    override fun listen(handleClient: suspend (ByteArray) -> Unit) {
        Log.d("DBG", "[UDP] Listening...")
    }

    override fun close() {
        Log.d("DBG", "[UDP] Closing...")
    }
}