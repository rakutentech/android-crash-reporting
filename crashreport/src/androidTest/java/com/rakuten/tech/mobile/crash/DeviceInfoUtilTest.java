package com.rakuten.tech.mobile.crash;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Instrumented unit tests for DeviceInfoUtil.java.
 */
@RunWith(AndroidJUnit4.class)
public class DeviceInfoUtilTest {

  private final int deviceInfoSize = 7;
  private final int deviceDetailSize = 7;
  private Context context;

  @Before
  public void setUp() {
    context = InstrumentationRegistry.getTargetContext();
  }

  /**
   * Test if device info would contain a fixed number of map elements.
   */
  @Test
  public void testDeviceInfoJsonObjectSize() {
    assertEquals(DeviceInfoUtil.getInstance().getDeviceInfo(context).size(), deviceInfoSize);
  }

  /**
   * Test if device details would contain a fixed number of map elements.
   */
  @Test
  public void testDeviceDetailsJsonObjectSize() {
    assertEquals(DeviceInfoUtil.getInstance().getDeviceDetails(context).size(), deviceDetailSize);
  }

  /**
   * Test if device info map contains the correct key fields.
   */
  @Test
  public void testDeviceInfoJsonObjectKeys() {
    HashMap<String, String> deviceInfoMap = (HashMap<String, String>) DeviceInfoUtil.getInstance()
        .getDeviceInfo(context);

    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.APP_ID));
    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.VERSION));
    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.DEVICE_ID));
    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.CARRIER));
    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.STATUS));
    assertTrue(deviceInfoMap.containsKey(CrashReportConstants.LOCALE));
  }

  /**
   * Test if device details map contains the correct key fields.
   */
  @Test
  public void testDeviceDetailsJsonObjectKeys() {
    HashMap<String, String> deviceDetailsMap = (HashMap<String, String>) DeviceInfoUtil
        .getInstance().getDeviceDetails(context);

    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.MAKE));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.MODEL));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.OS));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.OS_VERSION));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.PROCESSOR));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.CPU));
    assertTrue(deviceDetailsMap.containsKey(CrashReportConstants.MEMORY));
  }

  /**
   * Test if device details with null context will return an empty map.
   */
  @Test
  public void testDeviceDetailsNullContext() {
    HashMap<String, String> deviceDetailsMap = (HashMap<String, String>) DeviceInfoUtil
        .getInstance().getDeviceDetails(null);

    assertTrue(deviceDetailsMap != null);
  }

  /**
   * Test if device info with null context will return an empty map.
   */
  @Test
  public void testDeviceInfoNullContext() {
    HashMap<String, String> deviceInfoMap = (HashMap<String, String>) DeviceInfoUtil.getInstance()
        .getDeviceInfo(null);

    assertTrue(deviceInfoMap != null);
  }

  @Test
  public void testGetDeviceVersion() {
    // Arrange.
    StringBuilder versionBuilder = new StringBuilder();
    try {
      String versionName = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionName;

      int versionCode = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0).versionCode;

      versionBuilder
          .append(versionName)
          .append("_")
          .append(versionCode);

    } catch (NameNotFoundException e) {
      e.printStackTrace();
    }

    // Act.
    Map<String, String> map = DeviceInfoUtil.getInstance().getDeviceInfo(context);

    // Assert.
    assertTrue(versionBuilder.toString().equals(map.get("version")));
  }
}
