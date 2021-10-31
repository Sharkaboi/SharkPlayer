package com.sharkaboi.sharkplayer.common.extensions

import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

suspend fun <T : Any> tryCatching(block: suspend () -> TaskState<T>): TaskState<T> {
    return withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e)
            return@withContext TaskState.Failure<T>(e)
        }
    }
}