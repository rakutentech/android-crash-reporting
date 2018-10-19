package com.rakuten.tech.mobile.crash;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import com.rakuten.tech.mobile.crash.processors.ConfigProcessor;
import com.rakuten.tech.mobile.crash.processors.CrashReportProcessor;
import com.rakuten.tech.mobile.crash.processors.ReportInstallEventProcessor;
import com.rakuten.tech.mobile.crash.processors.SessionLifecycleProcessor;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import java.util.HashMap;
import java.util.Map;

/**
 *  Service thread handling crash reporting tasks asynchronously relative to the application main thread.
 */
public class TaskProcessor extends Service {

  private final static String TAG = "TaskProcessor";
  private final Map<CrashReportTask.TaskType, CrashReportProcessor> processors
      = new HashMap<CrashReportTask.TaskType, CrashReportProcessor>() {
    {
      put(CrashReportTask.TaskType.GET_CONFIG, ConfigProcessor.getInstance());
      put(CrashReportTask.TaskType.NEW_INSTALL, ReportInstallEventProcessor.getInstance());
      put(CrashReportTask.TaskType.FLUSH_LIFECYCLES, SessionLifecycleProcessor.getInstance());
      put(CrashReportTask.TaskType.FOREGROUND, SessionLifecycleProcessor.getInstance());
      put(CrashReportTask.TaskType.BACKGROUND, SessionLifecycleProcessor.getInstance());
    }
  };
  IBinder iBinder;

  @Override
  public IBinder onBind(Intent intent) {
    return iBinder;
  }

  @Override
  public void onCreate() {
    Thread crashReportingSdkThread = new Thread(new Runnable() {
      @Override
      public void run() {
        while (true) {
          process(CrashReportTaskQueue.getInstance().dequeue());
        }
      }
    });

    // Set handler to catch exceptions thrown by the SDK.
    crashReportingSdkThread.setUncaughtExceptionHandler(new CrashReportingExceptionHandler());
    crashReportingSdkThread.start();
  }

  /**
   * Executes a crash reporting task from the queue.
   */
  private void process(CrashReportTask task) {
    CrashReportProcessor processor = processors.get(task.getType());

    if (processor != null) {
      processor.processTask(this, task);
    } else {
      Log.e(TAG, "No processor found for the given task type: " + task.getType().toString());
    }
  }
}
