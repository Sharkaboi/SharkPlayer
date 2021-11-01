package com.sharkaboi.sharkplayer.common.util

sealed class TaskState<T : Any> {
    data class Success<T : Any>(val data: T) : TaskState<T>()
    data class Failure<T : Any>(val error: Exception) : TaskState<T>()

    fun toKotlinResult(): Result<T> {
        return when (this) {
            is Failure -> Result.failure(error)
            is Success -> Result.success(data)
        }
    }

    val isSuccess get() = this is Success

    val isFailure get() = this is Failure

    companion object {
        fun <T : Any> failureWithMessage(message: String): Failure<T> {
            return Failure(Exception(message))
        }
    }
}

