package com.migraineguard.app.core.data

/** Lightweight result wrapper for repository / AI calls. */
sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
}
