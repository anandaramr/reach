package com.project.reach.network.model

import java.net.InetAddress

data class ServiceAddress(
    val ip: InetAddress,
    val port: Int
)