package com.sharkaboi.sharkplayer.common.extensions

internal fun Boolean.toInt(): Int {
    return if (this) 1 else 0
}