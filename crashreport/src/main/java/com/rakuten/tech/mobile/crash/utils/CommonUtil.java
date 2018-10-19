package com.rakuten.tech.mobile.crash.utils;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rakuten.tech.mobile.crash.ApplicationState;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utility class containing common methods used by the SDK.
 */
public enum CommonUtil {
  INSTANCE;
  private final static String TAG = "CommonUtil";

  /**
   * Sets all system stats for session lifecycles.
   */
  public JSONArray getAllSystemStats(Context context) {
    // Set up system_stats.
    JSONArray systemStatsArray = new JSONArray();

    try {
      Map deviceDetails = DeviceInfoUtil.getInstance().getDeviceDetails(context);

      JSONObject systemStatThreads = new JSONObject();
      systemStatThreads.put(CrashReportConstants.SYS_KEY, CrashReportConstants.THREADS);
      systemStatThreads.put(CrashReportConstants.SYS_VALUE,
          CrashInfoUtil.getInstance().getStackTracesFromAllThreads());
      systemStatsArray.put(systemStatThreads);

      JSONObject systemStatOSVersion = new JSONObject();
      systemStatOSVersion.put(CrashReportConstants.SYS_KEY, CrashReportConstants.OS_VERSION);
      systemStatOSVersion.put(CrashReportConstants.SYS_VALUE,
          deviceDetails.get(CrashReportConstants.OS_VERSION));
      systemStatsArray.put(systemStatOSVersion);

      JSONObject systemStatDeviceModel = new JSONObject();
      systemStatDeviceModel.put(CrashReportConstants.SYS_KEY, CrashReportConstants.DEVICE_MODEL);
      systemStatDeviceModel.put(CrashReportConstants.SYS_VALUE,
          deviceDetails.get(CrashReportConstants.MODEL));
      systemStatsArray.put(systemStatDeviceModel);

      JSONObject systemStatTotalDiskSpace = new JSONObject();
      systemStatTotalDiskSpace
          .put(CrashReportConstants.SYS_KEY, CrashReportConstants.TOTAL_DISK_SPACE);
      systemStatTotalDiskSpace.put(CrashReportConstants.SYS_VALUE,
          DeviceInfoUtil.getInstance().getDiskSpaceDetails()[0]);
      systemStatsArray.put(systemStatTotalDiskSpace);

      JSONObject systemStatFreeDiskSpace = new JSONObject();
      systemStatFreeDiskSpace
          .put(CrashReportConstants.SYS_KEY, CrashReportConstants.FREE_DISK_SPACE);
      systemStatFreeDiskSpace.put(CrashReportConstants.SYS_VALUE,
          DeviceInfoUtil.getInstance().getDiskSpaceDetails()[1]);
      systemStatsArray.put(systemStatFreeDiskSpace);

      JSONObject systemStatTotalMemory = new JSONObject();
      systemStatTotalMemory.put(CrashReportConstants.SYS_KEY, CrashReportConstants.TOTAL_RAM);
      systemStatTotalMemory.put(CrashReportConstants.SYS_VALUE,
          DeviceInfoUtil.getInstance().getRAMDetails(context)[0]);
      systemStatsArray.put(systemStatTotalMemory);

      JSONObject systemStatFreeMemory = new JSONObject();
      systemStatFreeMemory.put(CrashReportConstants.SYS_KEY, CrashReportConstants.FREE_RAM);
      systemStatFreeMemory.put(CrashReportConstants.SYS_VALUE,
          DeviceInfoUtil.getInstance().getRAMDetails(context)[1]);
      systemStatsArray.put(systemStatFreeMemory);

      JSONObject systemStatIsAppInFocus = new JSONObject();
      systemStatIsAppInFocus.put(CrashReportConstants.SYS_KEY, CrashReportConstants.IS_APP_IN_FOCUS);
      systemStatIsAppInFocus.put(CrashReportConstants.SYS_VALUE,
          Boolean.toString(ApplicationState.INSTANCE.isAppInFocus));
      systemStatsArray.put(systemStatIsAppInFocus);

    } catch (JSONException e) {
      Log.e(TAG, "Failure to supply system stats for session lifecycles.", e);
    }

    return systemStatsArray;
  }

  /**
   * @return String containing the body of the connection response or
   *   null if the input stream could not be read correctly
   */
  @Nullable
  public String readInputStreamToString(InputStream inputStream) {
    String result;
    StringBuilder stringBuffer = new StringBuilder();

    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
      String inputLine = "";
      while ((inputLine = br.readLine()) != null) {
        stringBuffer.append(inputLine);
      }
      result = stringBuffer.toString();
    } catch (IOException e) {
      Log.i(TAG, "Error reading InputStream");
      result = null;
    } finally {
      if (inputStream != null) {
        try {
          inputStream.close();
        } catch (IOException e) {
          Log.d(TAG, "Error closing InputStream");
        }
      }
    }

    return result;
  }
}
