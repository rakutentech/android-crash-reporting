package com.rakuten.tech.mobile.crash.processors

import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldNotBeEmpty
import org.junit.Before
import java.util.Arrays
import org.junit.Test

class CustomLoggerSpec {

    private lateinit var log: CustomLogger

    @Before fun setup() {
        log = CustomLogger()
    }

    @Test
    @Throws(LogEntrySizeLimitExceededError::class)
    fun addNullCustomLog() {
        // Allow log messages set to null, but it will not be included in the list of logs.
        log.addCustomLog(null)

        log.customLogs.shouldBeEmpty()
    }

    @Test(expected = LogEntrySizeLimitExceededError::class)
    @Throws(LogEntrySizeLimitExceededError::class)
    fun addLargeCustomLog() {
        // Reject custom logs that reaches the 1KB limit or greater.
        val chars = CharArray(1000)
        Arrays.fill(chars, 'a')

        log.addCustomLog(String(chars))
    }

    @Test
    @Throws(LogEntrySizeLimitExceededError::class)
    fun noCustomLog() {
        log.customLogs.shouldBeEmpty()
    }

    @Test
    @Throws(LogEntrySizeLimitExceededError::class)
    fun addCustomLog() {
        log.addCustomLog("TEST_LOG")

        log.customLogs.shouldNotBeEmpty()
    }

    @Test
    @Throws(LogEntrySizeLimitExceededError::class)
    fun customLogsAddedInOrder() {
        // Allow insertion of at most 64 log messages.
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
    @Throws(LogEntrySizeLimitExceededError::class)
    fun arbitraryCustomLogsAddedInOrder() {
        // Allow inserting 70 log messages; Removes the oldest log messages after 64.
        for (i in 0..69) {
            log.addCustomLog(Integer.toString(i))
        }

        val logList = log.customLogs.split("\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        // Check that all keys are stored in expected order after exceeding 64 keys.
        for (i in 6..63) {
            // Assert true that the first 6 keys no longer contains 0 to 5, since they were removed.
            logList[i - 6].shouldBeEqualTo("$i")
        }
    }
}