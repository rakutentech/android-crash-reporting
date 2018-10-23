package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.rakuten.tech.mobile.crash.ApplicationState;
import com.rakuten.tech.mobile.crash.AsyncHttpPost;
import com.rakuten.tech.mobile.crash.CrashReport;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import com.rakuten.tech.mobile.crash.tasks.BackgroundTask;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.tasks.ForegroundTask;
import com.rakuten.tech.mobile.crash.utils.CommonUtil;
import com.rakuten.tech.mobile.crash.utils.CrashInfoUtil;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stores lifecycles that contain records of application start, end, and crashes times.
 */
public class SessionLifecycleProcessor implements CrashReportProcessor {

  private static final SessionLifecycleProcessor INSTANCE = new SessionLifecycleProcessor();
  private final static String TAG = "LifecycleProcessor";
  private boolean isAppInFocus = false;
  private long sessionStart = 0;
  private long sessionEnd = 0;

  public static SessionLifecycleProcessor getInstance() {
    return INSTANCE;
  }

  public void processTask(Context context, CrashReportTask task) {
    switch (task.getType()) {
      case FOREGROUND:
        setSessionStartTime(context, ((ForegroundTask) task).getTimestamp());
        break;
      case BACKGROUND:
        setSessionEndTime(((BackgroundTask) task).getTimestamp());
        break;
      case FLUSH_LIFECYCLES:
        flushLifecyclesData(context);
        break;
    }
  }

  /**
   * Reports crash info to server during an uncaught exception on the main application thread.
   *
   * @param ex Contains information related to the crash.
   * @param context Contains information related to the device and application.
   */
  public void reportCrash(Context context, @Nullable Throwable ex) {
    // Set the final application state at time of crash.
    // Captures whether the host app is in foreground or background.
    ApplicationState.INSTANCE.isAppInFocus = this.isAppInFocus;

    // Edge case in which app crashes before UI is rendered and before crash reporting queue is created.
    if (sessionStart == 0) {
      setSessionStartTime(context, System.currentTimeMillis());
    }

    //Add lifecycle containing the crash into the list of application lifecycles.
    setSessionEndTime(System.currentTimeMillis());
    addLifecycle(context, ex);

    // Sending the application crash to crash report server.
    sendApplicationLifecycles(context);
  }

  private SessionLifecycleProcessor() {
  }

  /**
   * Persists a lifecycle consisting of foreground and background timestamps
   * and a possible crash into local storage file.
   */
  private void addLifecycle(Context context, @Nullable Throwable exception) {
    JSONObject lifecycle = new JSONObject();

    try {
      lifecycle.put(CrashReportConstants.FG, sessionStart);
      lifecycle.put(CrashReportConstants.BG, sessionEnd);

      // Checks if any crash occurred when application is running.
      if (exception != null) {
        // Set up crash_details.
        JSONObject crashDetails = new JSONObject();
        crashDetails.put(CrashReportConstants.ORIGIN_ERROR,
            CrashInfoUtil.getInstance().getPackageAndClassName(context, exception));
        crashDetails.put(CrashReportConstants.STACK_TRACE,
            CrashInfoUtil.getInstance().getExceptionStackTrace(exception));

        // Set up app_events.
        JSONArray appEventsArray = new JSONArray();
        HashMap<String, String> customKeysMap = new HashMap<>(CustomKeyCache
            .getInstance().getCustomKeys());

        for (Map.Entry<String, String> customKey : customKeysMap.entrySet()) {
          JSONObject appEvent = new JSONObject();

          appEvent.put(CrashReportConstants.APP_KEY, customKey.getKey());
          appEvent.put(CrashReportConstants.APP_VALUE, customKey.getValue());
          appEventsArray.put(appEvent);
        }

        // Includes any saved custom logs as an app_event.
        String customLogs = CustomLogger.getInstance().getCustomLogs();
        if (!TextUtils.isEmpty(customLogs)) {
          JSONObject customLogEvent = new JSONObject();
          customLogEvent.put(CrashReportConstants.APP_KEY, CrashReportConstants.LOG);
          customLogEvent.put(CrashReportConstants.APP_VALUE, customLogs);
          appEventsArray.put(customLogEvent);
        }

        // Add app_events and system_stats into crash_details JSONObject.
        crashDetails.put(CrashReportConstants.APP_EVENTS, appEventsArray);
        crashDetails.put(CrashReportConstants.SYSTEM_STATS,
            CommonUtil.INSTANCE.getAllSystemStats(context));

        // Add crash detail into the current application's lifecycle.
        lifecycle.put(CrashReportConstants.CRASH_DETAILS, crashDetails);
      }
    } catch (JSONException e) {
      Log.e(TAG, "Failure to locally store application foreground, background, and crash "
              + "lifecycle.",
          e);
    }

    // Save lifecycle to application cache.
    writeLifecycleCache(context, lifecycle);
  }

  /**
   * Clears the lifecycle file from the application cache.
   *
   * @param context Contains application specific cache directory.
   */
  private void clearLifecycleCache(Context context) {
    (new File(context.getCacheDir(), CrashReportConstants.LIFECYCLE_FILE)).delete();
  }

  /**
   * Sends all app lifecycles to crash report server and clears local cache.
   *
   * @param context Contains application specific cache directory.
   */
  private void checkLifecycleCacheSize(Context context) {
    File cachedFile = new File(context.getCacheDir(), CrashReportConstants.LIFECYCLE_FILE);
    // Checks if cached file is greater than 1 KB.
    if (cachedFile.length() > 1000) {
      sendApplicationLifecycles(context);
    }
  }

  /**
   * Flushes all lifecycle data to crash report server.
   */
  private void flushLifecyclesData(Context context) {
    File cachedFile = new File(context.getCacheDir(), CrashReportConstants.LIFECYCLE_FILE);

    if (cachedFile.exists() && cachedFile.length() != 0) {
      sendApplicationLifecycles(context);
    }
  }

  /**
   * Reads the lifecycle cached file.
   *
   * @param context Contains application specific cache directory.
   * @return JSONArray containing all application lifecycles as JSONObjects.
   */
  private JSONArray readLifecycleCache(Context context) {
    JSONArray results = new JSONArray();

    try {
      // Reading cached lifecycle file and parsing returned String output by '##'.
      File cachedFile = new File(context.getCacheDir(), CrashReportConstants.LIFECYCLE_FILE);
      InputStream inputStream = new FileInputStream(cachedFile);
      String[] parsedFile = (CommonUtil.INSTANCE.readInputStreamToString(inputStream).split("##"));

      for (String jsonStr : parsedFile) {
        results.put(new JSONObject(jsonStr));
      }
    } catch (IOException e) {
      Log.e(TAG, "Failure to read application cache for lifecycles.", e);
    } catch (JSONException e) {
      Log.e(TAG, "Failure to convert lifecycles into JSONObjects.", e);
    }

    return results;
  }

  /**
   * Sends application lifecycles containing crash and/or crash-free user sessions to Crash Report Server.
   *
   * @param context Contains information related to the device and application.
   */
  private void sendApplicationLifecycles(Context context) {
    try {
      // Populate crashInfo.
      JSONObject crashInfo = new JSONObject(
          DeviceInfoUtil.getInstance().getDeviceIdentifiers(context));

      crashInfo.put(CrashReportConstants.LIFECYCLES, readLifecycleCache(context));

      AsyncHttpPost.CrashServerURL url = new AsyncHttpPost.CrashServerURL(
          new URL(CrashReport.getInstance().getSessionsUrl()));
      SharedPreferences.Editor sharedPreferenceEditor = PreferenceManager
          .getDefaultSharedPreferences(context).edit();

      // Checks for the status code of the post request.
      if (new AsyncHttpPost(url, crashInfo).execute().get() == 200) {
        // Clears lifecycle cache after being sent to crash report server.
        clearLifecycleCache(context);
        sharedPreferenceEditor.putBoolean(CrashReportConstants.FLUSH_LIFECYCLES, false).apply();
      } else {
        sharedPreferenceEditor.putBoolean(CrashReportConstants.FLUSH_LIFECYCLES, true).apply();
      }
    } catch (MalformedURLException e) {
      Log.e(TAG, "Failure to send crash report to server.", e);
    } catch (JSONException e) {
      Log.e(TAG, "Failure to report application lifecycles to server.", e);
    } catch (InterruptedException e) {
      Log.e(TAG, "InterruptedException while sending crash report to server.", e);
    } catch (ExecutionException e) {
      Log.e(TAG, "Failed to send crash report to server.", e);
    }
  }

  /**
   * Detects if start of an activity is the application running in foreground and sets the start time.
   */
  private void setSessionStartTime(Context context, Long newStartTime) {
    // Checks if the app booted up or returned from background for longer than 5 seconds.
    isAppInFocus = true;
    if (sessionStart == 0) {
      sessionStart = newStartTime;
    } else if (newStartTime - sessionEnd > 5000) {
      // Add current application lifecycle in LIFECYCLE cache.
      addLifecycle(context, null);
      // Check if cache has reached the file size limit.
      checkLifecycleCacheSize(context);
      // Reset start timer to flag a new start time session.
      sessionStart = newStartTime;
    }
  }

  /**
   * Detects if pause of an activity is the application moving in background and sets the session end time.
   */
  private void setSessionEndTime(Long newEndTime) {
    isAppInFocus = false;
    sessionEnd = newEndTime;
  }

  /**
   *  Persists lifecycle data using application internal storage.
   *
   * @param context Contains application specific cache directory.
   * @param lifecycle Most recent lifecycle data needed to be written in cache.
   */
  private void writeLifecycleCache(Context context, JSONObject lifecycle) {
    File cachedFile = new File(context.getCacheDir(), CrashReportConstants.LIFECYCLE_FILE);
    try {
      // Stores lifecycle inside application cache directory.
      FileWriter fileWriter = new FileWriter(cachedFile, true);
      fileWriter.write(lifecycle.toString() + "##");
      fileWriter.close();
    } catch (IOException e) {
      Log.e(TAG,
          "Failure to write application foreground, background, and crash lifecycle in local cache.",
          e);
    }
  }
}