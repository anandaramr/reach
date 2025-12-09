package com.project.reach.domain.models

import android.net.Uri

data class Media(
    val contentUri: Uri,
    val mimeType: String,
    val size: Long,
    val filename: String
)
