package com.sharkaboi.sharkplayer.common.extensions

import kotlin.time.Duration

fun Duration.getTimeString(): String {
    if (!this.isPositive()) {
        return "0s"
    }
    val hours = this.inWholeHours.toInt()
    val minutes = this.minus(Duration.hours(hours)).inWholeMinutes
    val seconds = this.minus(Duration.minutes(minutes)).inWholeSeconds
    return buildString {
        append(if (hours <= 0) String.emptyString else "${hours}hr")
        append(" ")
        append(if (minutes <= 0) String.emptyString else "${minutes}min")
        append(" ")
        append(if (seconds <= 0 || minutes > 0 || hours > 0) String.emptyString else "${seconds}s")
    }.trim()
}