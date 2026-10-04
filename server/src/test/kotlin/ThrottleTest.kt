package com.angelma

import com.angelma.ext.Throttle
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertTrue

class ThrottleTest {

    @Test
    fun `throttle spaces blocks according to the limit`() = runBlocking {
        val throttle = Throttle().apply { mbps = 8.0 }
        val start = System.nanoTime()
        repeat(3) { throttle.waitFor(500_000) }
        val elapsedMs = (System.nanoTime() - start) / 1_000_000
        assertTrue(elapsedMs in 900..1300, "took $elapsedMs ms")
    }

}