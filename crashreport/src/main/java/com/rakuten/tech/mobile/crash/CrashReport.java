package com.rakuten.tech.mobile.crash;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError;
import com.rakuten.tech.mobile.crash.processors.ConfigProcessor.OnConfigSuccessCallback;
import com.rakuten.tech.mobile.crash.processors.CustomLogProcessor;
import com.rakuten.tech.mobile.crash.exception.IllegalKeyValuePairException;
import com.rakuten.tech.mobile.crash.exception.KeyValuePairSizeExceededError;
import com.rakuten.tech.mobile.crash.exception.MaximumCapacityReachedError;
import com.rakuten.tech.mobile.crash.processors.CustomKeyProcessor;
import com.rakuten.tech.mobile.crash.processors.SessionLifecycleProcessor;
import com.rakuten.tech.mobile.crash.tasks.BackgroundTask;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.tasks.FlushLifecyclesTask;
import com.rakuten.tech.mobile.crash.tasks.ForegroundTask;
import com.rakuten.tech.mobile.crash.tasks.NewInstallTask;
import com.rakuten.tech.mobile.crash.tasks.GetConfigTask;
import com.rakuten.tech.mobile.crash.utils.DeviceInfoUtil;

/**
 * Reports monitor app events to Crash Report server.
 */
public class CrashReport {

  private static final CrashReport INSTANCE = new CrashReport();
  private final static String TAG = "CrashReport";
  private static Boolean crashReportEnabled;
  private boolean crashReportInitiated = false;
  private String reportInstallsUrl;
  private String reportSessionsUrl;

  @Nullable
  public String getInstallsUrl() {
    return reportInstallsUrl;
  }

  @Nullable
  public String getSessionsUrl() {
    return reportSessionsUrl;
  }

  public void log(String message) throws LogEntrySizeLimitExceededError {
    CustomLogProcessor.getInstance().addCustomLog(message);
  }

  public void setBoolean(String key, boolean value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {
    CustomKeyProcessor.getInstance().addCustomKey(key, Boolean.toString(value));
  }

  public void setDouble(String key, double value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {
    CustomKeyProcessor.getInstance().addCustomKey(key, Double.toString(value));
  }

  public void setFloat(String key, float value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {
    CustomKeyProcessor.getInstance().addCustomKey(key, Float.toString(value));
  }

  public void setInteger(String key, int value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {
    CustomKeyProcessor.getInstance().addCustomKey(key, Integer.toString(value));
  }

  public void setString(String key, String value)
      throws KeyValuePairSizeExceededError, IllegalKeyValuePairException, MaximumCapacityReachedError {
    CustomKeyProcessor.getInstance().addCustomKey(key, value);
  }

  public void removeCustomKey(String key) {
    CustomKeyProcessor.getInstance().removeCustomKey(key);
  }

  /**
   * Returns an instance of Crash Report.
   */
  public static CrashReport getInstance() {
    return INSTANCE;
  }

  /**
   * Starts the instance of Crash Report to run on user application and runs initialization sequence.
   */
  void init(Context context) {
    try {
      if (context != null) {
        initializeCrashReporting(context);
      }
    } catch (Exception e) {
      Log.e(TAG, "Crash reporting failed to initialize.", e);
    }
  }

  private CrashReport() {
  }

  private void initializeCrashReporting(Context context) throws Exception {
    if (!crashReportInitiated && NetworkStateReceiver.isNetworkAvailable(context)) {
      // Start on a new thread for crash reporting.
      runServiceThread(context);

      // Get Subscription ID.
      DeviceInfoUtil.getInstance().init(context);

      queueTaskInBackground(new GetConfigTask(new OnConfigSuccessCallback() {
        @Override
        public void onSuccess(Context context,
            boolean isSdkEnabled,
            String installsUrl,
            String sessionsUrl) {

          // Complete the instance of Crash Report after successful communication with config server.
          if (isSdkEnabled) {
            // Set crash report URLs
            reportInstallsUrl = installsUrl;
            reportSessionsUrl = sessionsUrl;

            // Set the app's exception handler to communicate with crash report servers.
            setUncaughtExceptionHandler(context);
            // Initial tasks to be done on application start.
            runTasks();
            // Record start and end sessions of app activity.
            monitorAppLifecycle(context);
          }

          // Set a flag to prevent multiple processes of crash report.
          crashReportInitiated = true;
        }
      }));

    } else {
      PreferenceManager.getDefaultSharedPreferences(context)
          .edit().putBoolean(CrashReportConstants.FAILED_INIT, Boolean.TRUE).apply();
    }
  }

  /**
   * Stores application start, end, and crashes during an application lifecycle.
   */
  private void monitorAppLifecycle(final Context context) {
    Application app = (Application) context.getApplicationContext();

    app.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
      @Override
      public void onActivityCreated(Activity activity, Bundle bundle) {
      }

      @Override
      public void onActivityStarted(Activity activity) {
        // Queues up a new foreground event task asynchronously.
        queueTaskInBackground(new ForegroundTask(System.currentTimeMillis()));
      }

      @Override
      public void onActivityPaused(Activity activity) {
        // Queues up a new background event task asynchronously.
        queueTaskInBackground(new BackgroundTask(System.currentTimeMillis()));
      }

      @Override
      public void onActivityResumed(Activity activity) {
      }

      @Override
      public void onActivityStopped(Activity activity) {
      }

      @Override
      public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
      }

      @Override
      public void onActivityDestroyed(Activity activity) {
      }
    });
  }

  /**
   * Queue crash reporting tasks asynchronously to ensure crash reporting does not affect the app performance on the main thread.
   */
  private void queueTaskInBackground(final CrashReportTask task) {
    new AsyncTask<Void, Void, Void>() {

      @Override
      protected Void doInBackground(Void... voids) {
        CrashReportTaskQueue.getInstance().enqueue(task);
        return null;
      }
    }.execute();
  }

  /**
   * Starts a new android service in the background that dedicates a new thread for crash reporting.
   */
  private void runServiceThread(Context context) {
    context.startService(new Intent(context, TaskProcessor.class));
  }

  /**
   * Queues a flush on any cached crash reporting lifecycles and report new app install on app boot up.
   */
  private void runTasks() {
    queueTaskInBackground(new FlushLifecyclesTask());
    queueTaskInBackground(new NewInstallTask());
    queueTaskInBackground(new ForegroundTask(System.currentTimeMillis()));
  }

  private void setUncaughtExceptionHandler(final Context context) {
    // Initialization of UncaughtExceptionHandler.
    final Thread.UncaughtExceptionHandler defaultUEH = Thread
        .getDefaultUncaughtExceptionHandler();
    Thread.UncaughtExceptionHandler reportingUncaughtExceptionHandler =
        new Thread.UncaughtExceptionHandler() {
          /**
           * Set up the handler for uncaught exceptions. If ex is an instance of
           * StackOverFlowError call Thread.sleep(100) to delay handling the exception by
           * default handler since the nature of a StackOverflowError would otherwise cause
           * VM to become unresponsive.
           */
          @Override
          public void uncaughtException(Thread thread, Throwable ex) {
            SessionLifecycleProcessor.getInstance().reportCrash(context, ex);

            if (ex instanceof StackOverflowError) {
              try {
                Thread.sleep(100);
              } catch (InterruptedException e) {
                Log.e(TAG, "Error while sending crash information to server.", e);
              }
            }
            defaultUEH.uncaughtException(thread, ex);
          }
        };
    Thread.setDefaultUncaughtExceptionHandler(reportingUncaughtExceptionHandler);
  }
}
