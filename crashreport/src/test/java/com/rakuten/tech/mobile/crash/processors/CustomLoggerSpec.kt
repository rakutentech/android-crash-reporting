package com.rakuten.tech.mobile.crash.processors

import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import java.util.*

class CustomLoggerSpec {

    private lateinit var log: CustomLogger

    @Before fun setup() {
        log = CustomLogger()
    }

    @Test
    fun `should initially be empty`() {
        log.customLogs.shouldBeEmpty()
    }

    @Test
    fun `should allow, but ignore, null logs`() {
        log.addCustomLog(null)

        log.customLogs.shouldBeEmpty()
    }

    @Test(expected = LogEntrySizeLimitExceededError::class)
    fun `should reject logs larger than 1kB`() {
        val chars = CharArray(1000)
        Arrays.fill(chars, 'a')

        log.addCustomLog(String(chars))
    }

    @Test
    fun `should not be empty after adding a non-null log`() {
        log.addCustomLog("TEST_LOG")

        log.customLogs.shouldNotBeEmpty()
    }

    @Test
    fun `should append logs in order`() {
        for (i in 0..63) {
            log.addCustomLog(Integer.toString(i))
        }

        val logList = log.customLogs.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Assert that 64 keys are stored in order.
        for (i in 0..63) {
            logList[i].shouldBeEqualTo("$i")
        }
    }

    @Test
    fun `should evict oldest logs if it reaches maximum capacity (64)`() {
        for (i in 0..69) {
            log.addCustomLog(Integer.toString(i))
        }

        val logList = log.customLogs.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        logList.size.shouldEqual(64)
        for (i in 0..5) { // evictions
            logList.shouldNotContain("$i")
        }
        for (i in 6..69) {
            logList[i - 6].shouldBeEqualTo("$i")
        }
    }
}