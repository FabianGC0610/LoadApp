package com.udacity.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TransferData(
    val fileName: String,
    val downloadStatus: String,
    val fileSize: String,
    val fileType: String,
    val fileUri: String,
) : Parcelable
