package com.project.reach.network.model

import java.net.InetAddress

data class NetworkPacket(
    val address: InetAddress,
    val payload: ByteArray
)
