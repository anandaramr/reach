package com.project.reach.ui.utils

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.project.reach.util.debug

fun saveFileToStorage(
    context: Context,
    sourceUri: Uri,
    fileName: String,
    mimeType: String
): Uri? {
    val values = ContentValues().apply {
        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
        put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        put(MediaStore.MediaColumns.RELATIVE_PATH, getRelativePath(mimeType))
        put(MediaStore.MediaColumns.IS_PENDING, 1)
    }

    val resolver = context.contentResolver
    val destUri = resolver.insert(getCollection(mimeType), values) ?: return null

    try {
        resolver.openInputStream(sourceUri)?.use { input ->
            resolver.openOutputStream(destUri)?.use { output ->
                input.copyTo(output)
            }
        }

        values.clear()
        values.put(MediaStore.MediaColumns.IS_PENDING, 0)
        resolver.update(destUri, values, null, null)
        debug("Downloaded file: $fileName")
        return destUri
    } catch (e: Exception) {
        debug("Error while downloading file: $e")
        resolver.delete(destUri, null, null)
        return null
    }
}

fun getRelativePath(mimeType: String): String {
    return when {
        mimeType.startsWith("image/") -> Environment.DIRECTORY_PICTURES + "/Reach"
        mimeType.startsWith("video/") -> Environment.DIRECTORY_MOVIES + "/Reach"
        mimeType.startsWith("audio/") -> Environment.DIRECTORY_MUSIC + "/Reach"
        else -> Environment.DIRECTORY_DOWNLOADS + "/Reach"
    }
}

fun getCollection(mimeType: String): Uri {
    return when {
        mimeType.startsWith("image/") -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        mimeType.startsWith("video/") -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        mimeType.startsWith("audio/") -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> MediaStore.Downloads.EXTERNAL_CONTENT_URI
        else -> MediaStore.Files.getContentUri("external")
    }
}