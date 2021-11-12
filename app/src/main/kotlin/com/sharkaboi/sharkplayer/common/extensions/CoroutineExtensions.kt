package com.sharkaboi.sharkplayer.common.extensions

import com.sharkaboi.sharkplayer.common.util.TaskState
import kotlinx.coroutines.*
import timber.log.Timber

suspend fun <T : Any> tryCatching(
    block: suspend () -> TaskState<T>
): TaskState<T> {
    return withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: Exception) {
            Timber.e(e)
            return@withContext TaskState.Failure<T>(e)
        }
    }
}

fun <T> debounce(
    delay: Long = 800L,
    scope: CoroutineScope,
    callback: (T?) -> Unit
): (T?) -> Unit {
    var debounceJob: Job? = null
    return { param: T? ->
        debounceJob?.cancel()
        debounceJob = scope.launch {
            delay(delay)
            callback(param)
        }
    }
}