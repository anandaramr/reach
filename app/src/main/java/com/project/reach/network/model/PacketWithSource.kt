package com.project.reach.network.model

import java.net.InetAddress

data class PacketWithSource(
    val packet: Packet,
    val sourceIp: InetAddress
)