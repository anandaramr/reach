package com.project.reach.data.utils

import java.io.File

data class PrivateFile(
    val hash: String,
    val file: File,
    val mimeType: String,
    val filename: String,
    val location: String
)
