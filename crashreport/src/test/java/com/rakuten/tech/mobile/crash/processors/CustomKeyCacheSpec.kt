package com.rakuten.tech.mobile.crash.processors

import org.junit.Assert.*

import com.rakuten.tech.mobile.crash.exception.IllegalKeyValuePairException
import com.rakuten.tech.mobile.crash.exception.KeyValuePairSizeExceededError
import com.rakuten.tech.mobile.crash.exception.MaximumCapacityReachedError
import org.amshove.kluent.*
import org.junit.Before
import java.util.Arrays
import org.junit.Test

class CustomKeyCacheSpec {

    private val key = "test_key1"
    private val value = "test_value"
    private lateinit var cache: CustomKeyCache

    @Before fun setup() {
        cache = CustomKeyCache()
    }

    @Test(expected = IllegalKeyValuePairException::class)
    @Throws(KeyValuePairSizeExceededError::class, IllegalKeyValuePairException::class, MaximumCapacityReachedError::class)
    fun addNullCustomKeyToMap1() {
        // Assert true that map will not contain null custom key.
        cache.addCustomKey(null, value)
    }

    @Test(expected = KeyValuePairSizeExceededError::class)
    @Throws(KeyValuePairSizeExceededError::class, IllegalKeyValuePairException::class, MaximumCapacityReachedError::class)
    fun rejectLargeCustomKeyFromMap() {
        // Reject custom key that reaches the 1KB limit or greater.
        val chars = CharArray(1000)
        Arrays.fill(chars, 'a')

        cache.addCustomKey(key, String(chars))
    }

    @Test
    @Throws(KeyValuePairSizeExceededError::class, IllegalKeyValuePairException::class, MaximumCapacityReachedError::class)
    fun addNullCustomKeyToMap() {
        // Assert true that map will contain custom key with a null string value.
        cache.addCustomKey(key, null)

        cache.customKeys.keys.shouldContain(key)
    }

    @Test
    fun removeNullKeyFromMap() {
        val originalMap = cache.customKeys.toMap()

        // Should not make any changes to the custom map.
        cache.removeCustomKey(null)

        cache.customKeys.shouldContainSame(originalMap)
    }

    @Test
    @Throws(KeyValuePairSizeExceededError::class, IllegalKeyValuePairException::class, MaximumCapacityReachedError::class)
    fun addAndRemoveCustomKeyFromMap() {
        // Assert true that custom key is inserted and map contains the key.
        cache.addCustomKey(key, value)
        cache.customKeys.shouldNotBeEmpty()

        // Assert false that map does contain key; After key removal by setting value to null.
        cache.removeCustomKey(key)
        cache.customKeys.keys.shouldNotContain(key)

        // Map contains size 0.
        cache.customKeys.shouldBeEmpty()
    }

    @Test
    @Throws(KeyValuePairSizeExceededError::class, IllegalKeyValuePairException::class, MaximumCapacityReachedError::class)
    fun getCustomValueFromMap() {
        // Assert true that custom key can be used to retrieve its value from map.
        cache.addCustomKey(key, value)

        assertTrue(cache.customKeys.containsKey(key))
        assertTrue(cache.customKeys[key] == value)
        assertTrue(cache.customKeys.size == 1)
    }
}