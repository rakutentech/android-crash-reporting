package com.rakuten.tech.mobile.crash.processors

import android.preference.PreferenceManager
import com.rakuten.tech.mobile.crash.CrashReportConstants
import com.rakuten.tech.mobile.crash.RobolectricUnitSpec
import com.rakuten.tech.mobile.crash.processors.ConfigProcessor.OnConfigSuccessCallback
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment

import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNullOrBlank

class ConfigProcessorSpec : RobolectricUnitSpec() {

    private val context = RuntimeEnvironment.application

    private var enabled: Boolean = false
    private var sessionEndpoint: String? = null
    private var installEndpoint: String? = null

    private val callback = OnConfigSuccessCallback { _, isSdkEnabled, sessionsUrl, installsUrl ->
        enabled = isSdkEnabled
        sessionEndpoint = sessionsUrl
        installEndpoint = installsUrl
    }

    @Before
    @Throws(JSONException::class)
    fun setUp() {
        enabled = false
        sessionEndpoint = null
        installEndpoint = null
        PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit()
    }

    // base of every config response
    private fun response(): JSONObject {
        return JSONObject("""
                    {
                        "endpoints": {
                            "install": "https://test.example.com/install",
                            "sessions": "https://test.example.com/sessions"
                        }
                    }
                    """)
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable() {
        // Check that crash report enabled on app start up - sdk enabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": true
            }
            """)

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckDisable() {
        // Check that crash report disabled on app start up - sdk disabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": false
            }
            """)

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)


        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testEmptyJSONServerConfig() {
        val data = response()

        // Disable SDK when missing required JSON fields.
        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCompleteJSONServerConfigs() {
        // Enables SDK with all required JSON fields and enabled as true.
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": true
            }
            """)

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testPartialJSONServerConfigs() {
        // test 1
        val data = response()
        // SDK is not enabled when missing any required JSON fields.
        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()

        // test 2
        data.merge(""" { "override": true } """)

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()

        // test 3
        data.merge(""" { "sticky": true } """)

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()

        // test 4
        data.merge(""" { "enabled": true } """)
        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable1() {
        // Overrides current sticky enabled false to true - sdk enabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": true
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, false).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable2() {
        // Overrides current sticky enabled true to false - sdk disabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": false
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, true).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable3() {
        // Overrides current sticky enabled true to true - sdk enabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": false,
                "enabled": true
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, true).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable4() {
        // Overrides current sticky enabled true to true - sdk enabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": false,
                "enabled": false
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, true).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable5() {
        // No override current sticky enabled false - sdk disabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": true,
                "enabled": true
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, false).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable6() {
        // No override current sticky enabled true - sdk enabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": true
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, true).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable7() {
        // No override current sticky enabled false - sdk disabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": true,
                "enabled": false
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, false).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    @Throws(JSONException::class, InterruptedException::class)
    fun testCheckEnable8() {
        // No override current sticky enabled true - sdk enabled.
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": false
            }
            """)

        // Preset a previous host app config.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
                .putBoolean(CrashReportConstants.STICKY, true).apply()

        ConfigProcessor.getInstance().updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    private fun JSONObject.merge(string: String): JSONObject {
        val other = JSONObject(string)
        for (key in other.keys()) {
            this.put(key, other.get(key))
        }
        return this
    }
}