package com.rakuten.tech.mobile.crash.processors

import android.content.SharedPreferences
import android.preference.PreferenceManager
import com.rakuten.tech.mobile.crash.CrashReportConstants
import com.rakuten.tech.mobile.crash.RobolectricUnitSpec
import com.rakuten.tech.mobile.crash.processors.ConfigProcessor.OnConfigSuccessCallback
import org.amshove.kluent.shouldBeFalse
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldNotBeNullOrBlank
import org.json.JSONException
import org.json.JSONObject
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class ConfigProcessorSpec : RobolectricUnitSpec() {

    private val context = RuntimeEnvironment.application

    private var enabled: Boolean = false
    private var sessionEndpoint: String? = null
    private var installEndpoint: String? = null

    private lateinit var prefs: SharedPreferences
    private lateinit var configProcessor: ConfigProcessor

    private val callback = OnConfigSuccessCallback { _, isSdkEnabled, sessionsUrl, installsUrl ->
        enabled = isSdkEnabled
        sessionEndpoint = sessionsUrl
        installEndpoint = installsUrl
    }

    private var sticky : Boolean
        set(value) = prefs.edit().putBoolean(CrashReportConstants.STICKY, value).apply()
        get() = prefs.getBoolean(CrashReportConstants.STICKY, false)


    @Before
    @Throws(JSONException::class)
    fun setUp() {
        enabled = false
        sessionEndpoint = null
        installEndpoint = null
        prefs = PreferenceManager.getDefaultSharedPreferences(context)

        configProcessor = ConfigProcessor()

        prefs.edit().clear().commit()
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
    fun `should enable when enable flag is true`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": true
            }
            """)

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    fun `should disable when enable flag is false`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": false
            }
            """)

        configProcessor.updateHostConfig(context, data, callback)


        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should disable when config flags are missing`() {
        val data = response()

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should enable when all config flags are true`() {
        // Enables SDK with all required JSON fields and enabled as true.
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": true
            }
            """)

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    fun `should disable when config flags are incomplete 1` () {
        val data = response()
        data.merge(""" { "override": true } """)

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should disable when config flags are incomplete 2` () {
        val data = response().merge("""
            {
                "override": true,
                "sticky": true
            }
            """)

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should override cached sticky with true when sticky flag is true`() {
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": true
            }
            """)
        sticky = false

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sticky.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    fun `should override cached sticky with false when enable flag is false`() {
        // Overrides current sticky enabled true to false - sdk disabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": true,
                "enabled": false
            }
            """)
        sticky = true

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sticky.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should override cached sticky with false when sticky flag is false`() {
        // Overrides current sticky enabled true to true - sdk enabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": false,
                "enabled": true
            }
            """)
        sticky = true

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sticky.shouldBeFalse()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    fun `should override cached sticky with false when sticky enable flags are false`() {
        // Overrides current sticky enabled true to true - sdk enabled.
        val data = response().merge("""
            {
                "override": true,
                "sticky": false,
                "enabled": false
            }
            """)
        sticky = true

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sticky.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should not override cached sticky and enable when override flage is false`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": true,
                "enabled": true
            }
            """)
        sticky = false

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sticky.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should not override cached sticky (true) when override is false 1`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": true
            }
            """)
        sticky = true

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sticky.shouldBeTrue()
        sessionEndpoint.shouldNotBeNullOrBlank()
        installEndpoint.shouldNotBeNullOrBlank()
    }

    @Test
    fun `should not override cached sticky (false) when override is false`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": true,
                "enabled": false
            }
            """)
        sticky = false

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeFalse()
        sticky.shouldBeFalse()
        sessionEndpoint.shouldBeNull()
        installEndpoint.shouldBeNull()
    }

    @Test
    fun `should not override cached sticky (true) when override is false 2`() {
        val data = response().merge("""
            {
                "override": false,
                "sticky": false,
                "enabled": false
            }
            """)
        sticky = true

        configProcessor.updateHostConfig(context, data, callback)

        enabled.shouldBeTrue()
        sticky.shouldBeTrue()
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