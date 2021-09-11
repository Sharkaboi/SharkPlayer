package com.sharkaboi.sharkplayer.common.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

fun Cursor.getStringOfColumnName(name: String): String {
    return try {
        this.getStringOrNull(this.getColumnIndexOrThrow(name)) ?: String.emptyString
    } catch (e: Exception) {
        String.emptyString
    }
}

fun Cursor.getLongOfColumnName(name: String): Long {
    return try {
        this.getStringOrNull(this.getColumnIndexOrThrow(name)) ?: "0"
    } catch (e: Exception) {
        "0"
    }.toLong()
}