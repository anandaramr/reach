package com.project.reach.ui.screens.qrcode

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import com.project.reach.domain.contracts.IIdentityRepository
import com.project.reach.util.debug
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class QRCodeViewModel @Inject constructor(
    private val identityRepository: IIdentityRepository,
): ViewModel() {
    fun getTempImageUri(context: Context, imageBitMap: Bitmap?): Uri {
        val imagesDir = File(context.cacheDir, "images")
        imagesDir.mkdirs()
        val file = File(imagesDir, "qr_code.png")
        FileOutputStream(file).use {
            imageBitMap?.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    }
}