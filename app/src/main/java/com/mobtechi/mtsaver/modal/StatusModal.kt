package com.mobtechi.mtsaver.modal

import android.net.Uri

data class StatusModal(
    val fileName: String,
    val fileType: String,
    val filePath: String,
    val fileUri: Uri
)
