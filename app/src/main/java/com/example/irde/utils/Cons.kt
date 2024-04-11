package com.example.irde.utils

import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.annotation.RequiresApi
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Cons {
    companion object {
        var BASE_URL = "http://164.52.223.163:4602"
        const val isImportFile = "isImportFile"
        const val isImportAsset = "isImportAsset"
        const val NOTFOUND = "Not Found"
        const val FOUND = "Found"

        @RequiresApi(Build.VERSION_CODES.O)
        fun getDatetime(text: String): String? {

            val time = LocalDateTime.now()
            val str_date = time.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"))

            return (str_date)
        }

    }



}