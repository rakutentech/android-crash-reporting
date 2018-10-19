package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import com.rakuten.tech.mobile.crash.AsyncHttpPost;
import com.rakuten.tech.mobile.crash.CrashReport;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Collects device specs and reports to crash report server.
 */
public class ReportInstallEventProcessor implements CrashReportProcessor {

  private static final ReportInstallEventProcessor INSTANCE = new ReportInstallEventProcessor();
  private final static String TAG = "ReportInstallProcessor";

  public static ReportInstallEventProcessor getInstance() {
    return INSTANCE;
  }

  public void processTask(Context context, CrashReportTask task) {
    attemptReportInstall(context);
  }

  private ReportInstallEventProcessor() {
  }

  /**
   * Checks for new app installation to report new device info.
   */
  private void attemptReportInstall(Context context) {
    // Checks by shared preferences if app is newly installed on device.
    if (PreferenceManager.getDefaultSharedPreferences(context)
        .getBoolean(CrashReportConstants.NEW_INSTALL, true)) {
      setDefaultSharedPreferences(context);
      sendInstallEvent(context);
    }
  }

  /**
   * Reports new app installation device info to the server.
   */
  private void sendInstallEvent(Context context) {
    try {
      JSONObject deviceInstance = new JSONObject(
          DeviceInfoUtil.getInstance().getDeviceInfo(context));
      deviceInstance.put(CrashReportConstants.DEVICE_INFO,
          new JSONObject(DeviceInfoUtil.getInstance().getDeviceDetails(context)));
      AsyncHttpPost.CrashServerURL url = new AsyncHttpPost.CrashServerURL(
          new URL(CrashReport.getInstance().getInstallsUrl()));
      // Checks for the status code of the post request.
      if (new AsyncHttpPost(url, deviceInstance).execute().get() == 200) {
        // Sets a flag indicating that the app is no longer new to the device.
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit().putBoolean(CrashReportConstants.NEW_INSTALL, false).apply();
      }
    } catch (JSONException e) {
      Log.e(TAG, "Error while reporting a new install event to Crash Report server.", e);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Failure to establish a connection with the Crash Report server.", e);
    } catch (InterruptedException e) {
      Log.e(TAG, "InterruptedException while sending crash report to server.", e);
    } catch (ExecutionException e) {
      Log.e(TAG, "Failed to send crash report to server.", e);
    }
  }

  /**
   * Configures shared preferences to a default state for a new app.
   */
  private void setDefaultSharedPreferences(Context context) {
    PreferenceManager
        .getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(CrashReportConstants.FLUSH_LIFECYCLES, false)
        .apply();
  }
}
