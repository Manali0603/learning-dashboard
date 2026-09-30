package com.example.intellipaatassignment.core

import kotlinx.coroutines.CancellationException
import java.io.IOException

/** Thrown by the (mock) network layer when the device has no internet connection. */
class NoConnectivityException : IOException("No internet connection")

/** Thrown by the (mock) auth API for a wrong email/password pair. */
class InvalidCredentialsException : Exception("Invalid email or password")

/**
 * Like [runCatching], but rethrows [CancellationException] so a cancelled coroutine
 * (e.g. the user leaving the screen) is never reported as a failure.
 */
suspend inline fun <T> suspendRunCatching(crossinline block: suspend () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

fun Throwable.toUserMessage(): String = when (this) {
    is NoConnectivityException -> "No internet connection. Check your network and try again."
    is InvalidCredentialsException -> "Invalid email or password"
    is IOException -> "Network error. Please try again."
    else -> "Something went wrong. Please try again."
}
