package com.rakuten.tech.mobile.crash

import android.content.pm.PackageManager.NameNotFoundException
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldEqual
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class DeviceInfoUtilSpec : RobolectricUnitSpec() {

    private val context = RuntimeEnvironment.application

    private val infoUtil = DeviceInfoUtil.getInstance()

    @Test
    fun `device info should have 7 entries`() {
        infoUtil.getDeviceInfo(context).size.shouldEqual(7)
    }

    @Test
    fun `device details should have 7 entries`() {
        infoUtil.getDeviceDetails(context).size.shouldEqual(7)
    }

    @Test
    fun `device info should contain expected keys`() {
        val deviceInfoMap = infoUtil.getDeviceInfo(context)


        deviceInfoMap.keys.shouldContain(CrashReportConstants.APP_ID)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.VERSION)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.DEVICE_ID)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.CARRIER)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.STATUS)
        deviceInfoMap.keys.shouldContain(CrashReportConstants.LOCALE)
    }

    @Test
    fun `device details should contain expected keys`() {
        val deviceDetailsMap = infoUtil.getDeviceDetails(context)

        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MAKE)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MODEL)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.OS)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.OS_VERSION)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.PROCESSOR)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.CPU)
        deviceDetailsMap.keys.shouldContain(CrashReportConstants.MEMORY)
    }

    @Test
    fun `device detail should be empty for null context`() {
        val deviceDetailsMap = infoUtil.getDeviceDetails(null)

        deviceDetailsMap.shouldBeEmpty()
    }

    @Test
    fun `device info should be empty for null context`() {
        val deviceInfoMap = infoUtil.getDeviceInfo(null)

        deviceInfoMap.shouldBeEmpty()
    }

    @Test
    fun `device version should be package versionName_versionCode`() {
        val version = try {
            val info = context.packageManager.getPackageInfo(context.packageName, 0)

            StringBuilder()
                    .append(info.versionName)
                    .append("_")
                    .append(info.versionCode)
                    .toString()
        } catch (e: NameNotFoundException) {
            ""
        }

        val deviceInfo = infoUtil.getDeviceInfo(context)

        deviceInfo["version"].shouldEqual(version)
    }
}
