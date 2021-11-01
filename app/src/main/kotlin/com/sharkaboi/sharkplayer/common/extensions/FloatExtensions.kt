package com.sharkaboi.sharkplayer.common.extensions

import kotlin.math.roundToInt

internal fun Float.nextEvenInt(): Int {
    return this.roundToInt().nextEven()
}