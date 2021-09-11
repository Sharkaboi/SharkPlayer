package com.sharkaboi.sharkplayer.common.extensions

import kotlin.time.Duration

fun Duration.getTimeString(): String {
    if (!this.isPositive()) {
        return "0s"
    }
    val hours = this.inWholeHours.toInt()
    val minutes = this.minus(Duration.hours(hours)).inWholeMinutes
    return if (hours <= 0) {
        "${minutes}m"
    } else {
        "${hours}h ${minutes}m"
    }
}