package com.sharkaboi.sharkplayer.common.util

sealed class TaskState<T> {
    data class Success<T>(val data: T) : TaskState<T>()
    data class Failure<T>(val error: Exception) : TaskState<T>()
    companion object {
        fun <T> failureWithMessage(message: String): Failure<T> {
            return Failure(Exception(message))
        }
    }
}

