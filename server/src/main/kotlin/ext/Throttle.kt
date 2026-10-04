package com.angelma.ext

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.max
import kotlin.time.Duration.Companion.milliseconds

class Throttle {

    @Volatile var mbps: Double = 0.0

    private val mutex = Mutex()
    private var nextFreeAtNanos = 0L

    suspend fun waitFor(bytes: Int) {
        val limit = mbps
        if (limit <= 0.0) return

        val waitNanos = mutex.withLock {
            val now = System.nanoTime()
            val start = max(now, nextFreeAtNanos)
            val bytesPerSecond = limit * 1_000_000 / 8
            val durationNanos = (bytes / bytesPerSecond * 1_000_000_000).toLong()
            nextFreeAtNanos = start + durationNanos
            start - now
        }

        if (waitNanos > 0) delay((waitNanos / 1_000_000).milliseconds)
    }
}