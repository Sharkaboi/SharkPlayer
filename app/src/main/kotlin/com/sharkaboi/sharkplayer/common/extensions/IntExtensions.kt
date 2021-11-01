package com.sharkaboi.sharkplayer.common.extensions

internal fun Int.nextEven(): Int {
    return this + (this % 2 != 0).toInt()
}