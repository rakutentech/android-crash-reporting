package com.rakuten.tech.mobile.crash

import org.json.JSONObject
import org.junit.Before
import org.junit.Ignore
import org.junit.runner.RunWith
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27])
@Ignore
open class RobolectricUnitSpec {
    @Before
    fun _setup() {
        MockitoAnnotations.initMocks(this)
    }
}