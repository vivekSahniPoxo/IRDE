package com.example.irde.utils

import java.text.SimpleDateFormat
import java.util.*

fun String.convertAndFormatDate(inputFormat: String, outputFormat: String): String {
    val inputDateFormat = SimpleDateFormat(inputFormat, Locale.getDefault())
    val date: Date = inputDateFormat.parse(this) ?: Date()

    val outputDateFormat = SimpleDateFormat(outputFormat, Locale.getDefault())
    return outputDateFormat.format(date)
}