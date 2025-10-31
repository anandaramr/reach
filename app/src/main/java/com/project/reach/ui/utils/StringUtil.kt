package com.project.reach.ui.utils

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