package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rakuten.tech.mobile.crash.AsyncHttpPost;
import com.rakuten.tech.mobile.crash.BuildConfig;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.tasks.GetConfigTask;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Retrieves and updates crash report SDK configurations from server.
 */
public class ConfigProcessor implements CrashReportProcessor {

  private final static String TAG = "ConfigProcessor";
  private final static ConfigProcessor INSTANCE = new ConfigProcessor();

  private ConfigProcessor() {
  }

  public static ConfigProcessor getInstance() {
    return INSTANCE;
  }

  @Override
  public void processTask(Context context, CrashReportTask task) {
    updateHostConfig(context, getServerConfig(context), ((GetConfigTask) task).getCallback());
  }

  /**
   * Update crash reporting with server configurations.
   */
  void updateHostConfig(Context context, JSONObject serverConfig,
      OnConfigSuccessCallback callback) {

    try {
      if (serverConfig == null
          || !serverConfig.has(CrashReportConstants.STICKY)
          || !serverConfig.has(CrashReportConstants.OVERRIDE)
          || !serverConfig.has(CrashReportConstants.ENABLED)
          || !serverConfig.getJSONObject(CrashReportConstants.ENDPOINTS)
          .has(CrashReportConstants.INSTALL)
          || !serverConfig.getJSONObject(CrashReportConstants.ENDPOINTS)
          .has(CrashReportConstants.SESSIONS)) {

        callback.onSuccess(context,
            false /* isSdkEnabled */,
            null /* reportInstallsUrl */,
            null /* reportSessionsUrl */);

        return;
      }

      boolean isEnabled;

      // Checks if new configurations is an override.
      if (serverConfig.getBoolean(CrashReportConstants.OVERRIDE)) {

        // If true, set STICKY enabled value in preferences to the value of enabled defined by the server.
        if (serverConfig.getBoolean(CrashReportConstants.STICKY)) {

          setConfigPreferences(context,
              CrashReportConstants.STICKY,
              serverConfig.getBoolean(CrashReportConstants.ENABLED));
        } else {
          PreferenceManager.getDefaultSharedPreferences(context).edit().remove
              (CrashReportConstants.STICKY).apply();
        }

        isEnabled = serverConfig.getBoolean(CrashReportConstants.ENABLED);

      } else {

        // Check if previous configuration does contain a STICKY value.
        if (PreferenceManager.getDefaultSharedPreferences(context)
            .contains(CrashReportConstants.STICKY)) {

          // Enable the SDK using the value from STICKY.
          isEnabled = PreferenceManager
              .getDefaultSharedPreferences(context)
              .getBoolean(CrashReportConstants.STICKY, Boolean.FALSE);
        } else {

          // If true, set STICKY enabled value in cache to the value of enabled defined by the server.
          if (serverConfig.getBoolean(CrashReportConstants.STICKY)) {
            setConfigPreferences(context,
                CrashReportConstants.STICKY,
                serverConfig.getBoolean(CrashReportConstants.ENABLED));
          }

          // Set enabled value to the value defined by the server.
          isEnabled = serverConfig.getBoolean(CrashReportConstants.ENABLED);
        }
      }

      String reportInstallsUrl = null;
      String reportSessionsUrl = null;

      if (isEnabled) {
        // Set crash reporting endpoints in memory.
        reportInstallsUrl = serverConfig
            .getJSONObject(CrashReportConstants.ENDPOINTS)
            .getString(CrashReportConstants.INSTALL);

        reportSessionsUrl = serverConfig
            .getJSONObject(CrashReportConstants.ENDPOINTS)
            .getString(CrashReportConstants.SESSIONS);
      }

      callback.onSuccess(context, isEnabled, reportInstallsUrl, reportSessionsUrl);

    } catch (JSONException e) {
      Log.e(TAG, "Unable to update the host configurations with new server changes.", e);
    }
  }

  /**
   * Retrieves SDK configurations from crash report server.
   */
  @Nullable
  private JSONObject getServerConfig(Context context) {
    JSONObject deviceIdentifiers = new JSONObject(DeviceInfoUtil
        .getInstance()
        .getDeviceIdentifiers(context));

    try {
      deviceIdentifiers.put(CrashReportConstants.SDK_VERSION, BuildConfig.VERSION_NAME);
      // Renames device VERSION key 'version' to 'app_version'.
      deviceIdentifiers.put(CrashReportConstants.APP_VERSION, deviceIdentifiers.get
          (CrashReportConstants.VERSION));
      deviceIdentifiers.remove(CrashReportConstants.VERSION);

      AsyncHttpPost.CrashServerURL url = new AsyncHttpPost.CrashServerURL(
          new URL(BuildConfig.CR_CONFIG));
      AsyncHttpPost request = new AsyncHttpPost(url, deviceIdentifiers);

      // Checks for the status code of the post request.
      if (request.execute().get() == 200) {

        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .remove(CrashReportConstants.FAILED_INIT).apply();

        return new JSONObject(request.getServerMessage())
            .getJSONObject(CrashReportConstants.DATA);
      } else {
        // Set flag indicating a failure to initiate crash report SDK.
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(CrashReportConstants.FAILED_INIT, true).apply();
      }

    } catch (JSONException e) {
      Log.e(TAG, "Failed to create a JSONObject with server configurations", e);
    } catch (MalformedURLException e) {
      Log.e(TAG, "Failure to establish a connection with the Config server.", e);
    } catch (InterruptedException e) {
      Log.e(TAG, "InterruptedException while connecting with the Config server.", e);
    } catch (ExecutionException e) {
      Log.e(TAG, "Failed to connect with the Config server.", e);
    }

    return deviceIdentifiers;
  }

  /**
   * Set server configs as a key value pair to the app's shared preferences.
   */
  private void setConfigPreferences(Context context, String key, Boolean value) {
    PreferenceManager
        .getDefaultSharedPreferences(context)
        .edit()
        .putBoolean(key, value)
        .apply();
  }

  /**
   * Callback interface to trigger code after successful communication to config server.
   */
  public interface OnConfigSuccessCallback {

    void onSuccess(Context context,
        boolean isSdkEnabled,
        @Nullable String reportInstallsUrl,
        @Nullable String reportSessionsUrl);
  }
}
