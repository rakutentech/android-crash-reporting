package com.rakuten.tech.mobile.crash.processors

import com.rakuten.tech.mobile.crash.exception.IllegalKeyValuePairException
import com.rakuten.tech.mobile.crash.exception.KeyValuePairSizeExceededError
import org.amshove.kluent.*
import org.junit.Before
import org.junit.Test
import java.util.*

class CustomKeyCacheSpec {

    private val key = "test_key1"
    private val value = "test_value"
    private lateinit var cache: CustomKeyCache

    @Before fun setup() {
        cache = CustomKeyCache()
    }

    @Test(expected = IllegalKeyValuePairException::class)
    fun `should not accept null as key`() {
        cache.addCustomKey(null, value)
    }

    @Test(expected = KeyValuePairSizeExceededError::class)
    fun `should reject key-value pairs with over 1 kB`() {
        val chars = CharArray(1000)
        Arrays.fill(chars, 'a')

        cache.addCustomKey(key, String(chars))
    }

    @Test
    fun `should accept null values`() {
        cache.addCustomKey(key, null)

        cache.customKeys.keys.shouldContain(key)
    }

    @Test
    fun `remove null key should not change the cache content`() {
        val originalMap = cache.customKeys.toMap()

        // Should not make any changes to the custom map.
        cache.removeCustomKey(null)

        cache.customKeys.shouldContainSame(originalMap)
    }

    @Test
    fun `should add key value pair`() {
        cache.addCustomKey(key, value)

        cache.customKeys.shouldNotBeEmpty()
        cache.customKeys.keys.shouldContain(key)
        cache.customKeys[key].shouldEqual(value)
    }

    @Test
    fun `should remove key value pair`() {
        cache.addCustomKey(key, value)

        cache.removeCustomKey(key)

        cache.customKeys.keys.shouldNotContain(key)
        cache.customKeys.shouldBeEmpty()
    }
}