package com.andrewlevada.togglewrapper.service

import java.text.SimpleDateFormat
import java.util.*

fun timeFromISO8601(iso8601: String): Long {
    val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT).parse(iso8601)
        ?: throw IllegalArgumentException("Invalid ISO8601 date: $iso8601")
    return date.time
}

fun getDisplayDurationOfTimeEntry(timeEntry: ToggleTimeEntry): String {
    val duration = System.currentTimeMillis() / 1000 + timeEntry.duration.toLong()
    val minutes = duration / 60
    val seconds = duration % 60
    return "$minutes:$seconds"
}

fun formatTime(minutes: Int, seconds: Int): String {
    val minutesString = if (minutes < 10) "0$minutes" else minutes.toString()
    val secondsString = if (seconds < 10) "0$seconds" else seconds.toString()
    return "$minutesString:$secondsString"
}