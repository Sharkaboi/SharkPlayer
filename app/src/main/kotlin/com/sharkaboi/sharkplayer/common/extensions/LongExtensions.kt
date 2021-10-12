package com.sharkaboi.sharkplayer.common.extensions

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

internal fun Long.getSizeString(): String {
    if (this <= 0) return "0B"

    val units = listOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

    val formatter = DecimalFormat("#,##0.#")
    val rawValue = this / 1024.0.pow(digitGroups.toDouble())

    return "${formatter.format(rawValue)} ${units[digitGroups]}"
}