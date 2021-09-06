package com.sharkaboi.sharkplayer.common.extensions

import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

internal fun Long.getSizeString(): String {
    if (this <= 0) return "0B"

    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()

    return DecimalFormat("#,##0.#").format(this / 1024.0.pow(digitGroups.toDouble()))
        .toString() + " " + units[digitGroups]
}