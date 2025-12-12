package com.project.reach.util

import com.google.protobuf.ByteString
import java.util.UUID

fun String.truncate(max: Int): String {
    return if (this.length <= max) {
        this
    } else {
        this.take(max) + "..."
    }.replace('\n', ' ')
}

fun String.toUUID(): UUID {
    return UUID.fromString(this)
}

fun String.toProtoBytes(): ByteString {
    val bytes = this.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    return ByteString.copyFrom(bytes)
}

fun ByteString.toHexString(): String {
    return this.toByteArray().joinToString("") { "%02x".format(it) }
}