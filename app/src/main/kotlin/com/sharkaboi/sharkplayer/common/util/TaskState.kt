package com.sharkaboi.sharkplayer.common.util

sealed class TaskState<T : Any> {
    data class Success<T : Any>(val data: T) : TaskState<T>()
    data class Failure<T : Any>(val error: Exception) : TaskState<T>()
    companion object {
        fun <T : Any> failureWithMessage(message: String): Failure<T> {
            return Failure(Exception(message))
        }
    }
}

