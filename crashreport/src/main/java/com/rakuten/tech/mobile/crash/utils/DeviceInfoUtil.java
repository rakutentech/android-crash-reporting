package com.rakuten.tech.mobile.crash.utils;

import static android.content.Context.ACTIVITY_SERVICE;

import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.google.android.gms.iid.InstanceID;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Utility class used to aggregate the user's device specifications.
 */
public class DeviceInfoUtil {

  private final static String TAG = "DeviceInfoUtil";
  private static final DeviceInfoUtil INSTANCE = new DeviceInfoUtil();

  private String apiKey = "";

  private DeviceInfoUtil() {
  }

  public static DeviceInfoUtil getInstance() {
    return INSTANCE;
  }

  /**
   * Lookup API key, and store it in memory.
   *
   * @param context required to collect information about the specific android device.
   */
  public void init(@NonNull Context context) {
    if (TextUtils.isEmpty(apiKey)) {
      try {
        Bundle metaData = context
            .getPackageManager()
            .getApplicationInfo(
                context.getPackageName(),
                PackageManager.GET_META_DATA)
            .metaData;
        apiKey = metaData.getString(CrashReportConstants.SUBSCRIPTION_KEY);
      } catch (PackageManager.NameNotFoundException e) {
        Log.e(TAG, e.getMessage());
      }
    }
  }

  /**
   * Gets the user's device identifiers.
   *
   * @param context required to collect information about the specific android device.
   * @return Map containing Android device and app specifications.
   */
  @NonNull
  public Map<String, String> getDeviceIdentifiers(Context context) {
    Map<String, String> deviceIdentifiers = new HashMap<>();

    if (context == null) {
      return deviceIdentifiers;
    }

    // Application package name.
    deviceIdentifiers.put(CrashReportConstants.APP_ID, context.getPackageName());

    // Host app's version which consists of versionName and versionCode in the format of
    // versionName_versionCode
    try {
      StringBuilder versionBuilder = new StringBuilder();
      String versionName = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0)
          .versionName;

      int versionCode = context.getPackageManager()
          .getPackageInfo(context.getPackageName(), 0)
          .versionCode;

      versionBuilder
          .append(versionName)
          .append("_")
          .append(versionCode);

      deviceIdentifiers.put(
          CrashReportConstants.VERSION,
          versionBuilder.toString());
    } catch (PackageManager.NameNotFoundException e) {
      Log.e(TAG, "Failed to retrieve application OS version.", e);
    }

    // Device identification generated by Google Play Services.
    deviceIdentifiers.put(CrashReportConstants.DEVICE_ID, InstanceID.getInstance(context).getId());

    // Device's operating system.
    deviceIdentifiers.put(CrashReportConstants.PLATFORM, CrashReportConstants.ANDROID);

    return deviceIdentifiers;
  }

  /**
   * Gets the user's device hardware and software specifications.
   *
   * @param context required to collect information about the specific android device.
   * @return Map containing Android device and app specifications.
   */
  @NonNull
  public Map<String, String> getDeviceInfo(Context context) {
    Map<String, String> deviceInfo = getDeviceIdentifiers(context);

    if (context == null) {
      return deviceInfo;
    }

    // Device phone service provider.
    deviceInfo.put(CrashReportConstants.CARRIER,
        ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE))
            .getNetworkOperatorName());

    // Set to active for a new app installation.
    deviceInfo.put(CrashReportConstants.STATUS, "ACTIVE");

    // Device's country and language default configuration.
    String locale = Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry();
    deviceInfo.put(CrashReportConstants.LOCALE, locale);

    return deviceInfo;
  }

  /**
   * Gets the device hardware and OS information.
   *
   * @param context required to collect information about the specific android device.
   * @return Map containing device information.
   */
  @NonNull
  public Map<String, String> getDeviceDetails(Context context) {
    Map<String, String> deviceDetails = new HashMap<>();

    if (context == null) {
      return deviceDetails;
    }

    // Company make.
    deviceDetails.put(CrashReportConstants.MAKE, Build.MANUFACTURER);

    // Phone model.
    deviceDetails.put(CrashReportConstants.MODEL, Build.MODEL);

    // Device's operating system.
    deviceDetails.put(CrashReportConstants.OS, CrashReportConstants.ANDROID);

    // Device's operating system numeric version.
    deviceDetails.put(CrashReportConstants.OS_VERSION, Build.VERSION.RELEASE);

    // CPU architecture name.
    deviceDetails.put(CrashReportConstants.PROCESSOR, System.getProperty("os.arch"));

    // CPU clock speed in GHz.
    deviceDetails.put(CrashReportConstants.CPU, getCpuInfo());

    // Total RAM size converted to bytes.
    deviceDetails.put(CrashReportConstants.MEMORY, getRAMDetails(context)[0]);

    return deviceDetails;
  }

  public String getApiKey() {
    return apiKey;
  }

  /**
   * Gets the disk space information from the device.
   * diskInfo[0] = Total Disk Space
   * diskInfo[1] = Free Disk Space
   *
   * @return String containing free disk space in bytes.
   */
  public String[] getDiskSpaceDetails() {
    StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
    String[] diskInfo = new String[2];

    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
      diskInfo[0] = Long.toString(statFs.getTotalBytes());
      diskInfo[1] = Long.toString(statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong());
    } else {
      diskInfo[0] = Long.toString(statFs.getBlockCount() * statFs.getBlockSize());
      diskInfo[1] = Long.toString(statFs.getAvailableBlocks() * statFs.getBlockSize());
    }

    return diskInfo;
  }

  /**
   * Gets the max CPU speed. Note: Android Emulator will not return a value for CPU speed.
   *
   * @return String containing the CPU speed in GHz.
   */
  private String getCpuInfo() {
    String cpuInfo = "";

    try {
      File file = new File(CrashReportConstants.CPUINFO_FILE);
      // Checks if the device records in a file its CPU speed information.
      if (file.isFile()) {
        FileInputStream fileStream = new FileInputStream(file);
        String temp = CommonUtil.INSTANCE.readInputStreamToString(fileStream);

        // Convert CPU speed from Hz to GHz.
        cpuInfo = Double.toString(Double.parseDouble(temp) / 1000000) + " GHz";
        fileStream.close();
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to receive the CPU clock speed from the device.", e);
    }

    return cpuInfo;
  }

  /**
   * Gets the memory information from the device.
   * memory[0] = Total Memory
   * memory[1] = Free Memory
   *
   * @return String array containing total memory and available memory.
   */
  public String[] getRAMDetails(Context context) {
    long B_PER_KB = 0x400L;
    String[] memory = new String[2];

    try {
      File file = new File(CrashReportConstants.MEMORYINFO_FILE); // File containing total RAM
      MemoryInfo memoryStats = new ActivityManager.MemoryInfo(); // File containing available RAM

      // Checks if the device records in a file its memory information.
      if (file.isFile()) {
        ((ActivityManager) context.getSystemService(ACTIVITY_SERVICE)).getMemoryInfo(memoryStats);
        BufferedReader memoryFileReader = new BufferedReader(new FileReader(file));

        String[] memoryInfo;

        // Reads the first line from file that contains the device's total memory.
        memoryInfo = memoryFileReader.readLine().split("\\s+");
        // Total memory size converted from KB to bytes.
        memory[0] = Long.toString(Long.parseLong(memoryInfo[1]) * B_PER_KB);
        // Available memory size in bytes.
        memory[1] = Long.toString(memoryStats.availMem);
      }
    } catch (IOException e) {
      Log.e(TAG, "Failed to receive RAM information from the device.", e);
    }

    return memory;
  }
}
