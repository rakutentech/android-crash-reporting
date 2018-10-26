package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
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

  @VisibleForTesting
  ConfigProcessor() {
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
      if(!hasAll(serverConfig,
          CrashReportConstants.STICKY, CrashReportConstants.OVERRIDE, CrashReportConstants.ENABLED)
          || !hasAll(serverConfig.getJSONObject(CrashReportConstants.ENDPOINTS),
              CrashReportConstants.INSTALL, CrashReportConstants.SESSIONS)) {

        callback.onSuccess(context,
            false /* isSdkEnabled */,
            null /* reportInstallsUrl */,
            null /* reportSessionsUrl */);

        return;
      }

      boolean isEnabled;
      boolean serverOverride = serverConfig.getBoolean(CrashReportConstants.OVERRIDE);
      boolean serverSticky = serverConfig.getBoolean(CrashReportConstants.STICKY);
      boolean serverEnable = serverConfig.getBoolean(CrashReportConstants.ENABLED);
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

      // Checks if new configurations is an override.
      if (serverOverride) {

        // If true, set STICKY enabled value in preferences to the value of enabled defined by the server.
        if (serverSticky) {
          prefs.edit().putBoolean(CrashReportConstants.STICKY, serverEnable).apply();
        } else {
          prefs.edit().remove(CrashReportConstants.STICKY).apply();
        }

        isEnabled = serverEnable;
      } else {

        // Check if previous configuration does contain a STICKY value.
        if (prefs.contains(CrashReportConstants.STICKY)) {

          // Enable the SDK using the value from STICKY.
          isEnabled = prefs.getBoolean(CrashReportConstants.STICKY, Boolean.FALSE);
        } else {

          // If true, set STICKY enabled value in cache to the value of enabled defined by the server.
          if (serverSticky) {
            prefs.edit().putBoolean(CrashReportConstants.STICKY, serverEnable).apply();
          }

          // Set enabled value to the value defined by the server.
          isEnabled = serverEnable;
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

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  private boolean hasAll(@Nullable JSONObject json, @NonNull String... keys) {
    if(json == null) {
      return false;
    }
    for(String key: keys) {
      if(!json.has(key)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Retrieves SDK configurations from crash report server.
   */
  @Nullable
  private JSONObject getServerConfig(Context context) {
    JSONObject deviceIdentifiers = new JSONObject(DeviceInfoUtil
        .getInstance().getDeviceIdentifiers(context));

    try {
      deviceIdentifiers.put(CrashReportConstants.SDK_VERSION, BuildConfig.VERSION_NAME);
      // Renames device VERSION key 'version' to 'app_version'.
      deviceIdentifiers.put(CrashReportConstants.APP_VERSION, deviceIdentifiers.get
          (CrashReportConstants.VERSION));
      deviceIdentifiers.remove(CrashReportConstants.VERSION);

      AsyncHttpPost.CrashServerURL url =
          new AsyncHttpPost.CrashServerURL(new URL(BuildConfig.CR_CONFIG));
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
   * Callback interface to trigger code after successful communication to config server.
   */
  public interface OnConfigSuccessCallback {

    void onSuccess(Context context,
        boolean isSdkEnabled,
        @Nullable String reportInstallsUrl,
        @Nullable String reportSessionsUrl);
  }
}
