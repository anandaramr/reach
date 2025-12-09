package com.project.reach.data.utils

import java.io.File

data class IngestResult(
    val hash: String,
    val file: File,
    val mimeType: String,
    val filename: String,
    val location: String
)
