package com.example.filesize_poc.utils

import android.content.Context
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object Utils {
//    fun getFile(context: Context, extension: String): File {
//        val timeStamp = SimpleDateFormat.getDateTimeInstance().format(Date())
//
//        return File.createTempFile(
//            "${extension.uppercase()}${timeStamp}",
//            ".$extension",
//            context.cacheDir
//        )
//    }

    fun getFile(context: Context?): File {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val date: String = formatter.format(Date()).toString()
        return File(context?.filesDir, "$date.png")
    }
}

val File.size get() = if (!exists()) 0.0 else length().toDouble()
val File.sizeInKb get() = size / 1024
val File.sizeInMb get() = sizeInKb / 1024

