package com.project.reach.ui.utils

fun String.truncate(max: Int): String {
    return if (this.length <= max) {
        this
    } else {
        this.take(max) + "..."
    }.replace('\n', ' ')
}