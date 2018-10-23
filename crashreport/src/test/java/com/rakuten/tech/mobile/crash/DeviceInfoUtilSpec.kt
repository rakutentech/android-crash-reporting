package com.rakuten.tech.mobile.crash

import android.content.pm.PackageManager.NameNotFoundException
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = intArrayOf(27))
class DeviceInfoUtilSpec {

    private val deviceInfoSize = 7
    private val deviceDetailSize = 7
    private val context = RuntimeEnvironment.application

    private val infoUtil = DeviceInfoUtil.getInstance()

    /**
     * Test if device info would contain a fixed number of map elements.
     */
    @Test
    fun testDeviceInfoJsonObjectSize() {
        infoUtil.getDeviceInfo(context).size.shouldEqual(deviceInfoSize)
    }

    /**
     * Test if device details would contain a fixed number of map elements.
     */
    @Test
    fun testDeviceDetailsJsonObjectSize() {
        infoUtil.getDeviceDetails(context).size.shouldEqual(deviceDetailSize)
    }

    /**
     * Test if device info map contains the correct key fields.
     */
    @Test
    fun testDeviceInfoJsonObjectKeys() {
        val deviceInfoMap = infoUtil.getDeviceInfo(context)


        deviceInfoMap.keys.shouldContain(CrashReportConstants.APP_ID)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.VERSION)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.DEVICE_ID)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.CARRIER)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.STATUS)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.LOCALE)
    }

    /**
     * Test if device details map contains the correct key fields.
     */
    @Test
    fun testDeviceDetailsJsonObjectKeys() {
        val deviceDetailsMap = infoUtil.getDeviceDetails(context)

        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MAKE)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MODEL)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.OS)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.OS_VERSION)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.PROCESSOR)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.CPU)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MEMORY)
    }

    /**
     * Test if device details with null context will return an empty map.
     */
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testDeviceDetailsNullContext() {
        val deviceDetailsMap = infoUtil.getDeviceDetails(null)
    }

    /**
     * Test if device info with null context will return an empty map.
     */
    @Suppress("UNUSED_VARIABLE")
    @Test
    fun testDeviceInfoNullContext() {
        val deviceInfoMap = infoUtil.getDeviceInfo(null)
    }

    @Test
    fun testGetDeviceVersion() {
        // Arrange.
        val version = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)

            StringBuilder()
                    .append(info.versionName)
                    .append("_")
                    .append(info.versionCode)
                    .toString()

        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            ""
        }

        // Act.
        val deviceInfo = infoUtil.getDeviceInfo(context)

        // Assert.
        deviceInfo["version"].shouldEqual(version)
    }
}
