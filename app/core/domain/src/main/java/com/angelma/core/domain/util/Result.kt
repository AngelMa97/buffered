package com.angelma.core.domain.util

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: com.angelma.core.domain.util.Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> =
    when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Error -> Result.Error(error)
    }

fun <T, E: Error> Result<T, E>.asEmptyDataResult(): EmptyResult<E> = map {}

typealias EmptyResult<E> = Result<Unit, E>