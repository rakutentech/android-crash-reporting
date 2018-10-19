package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Processor for adding in-memory arbitrary log messages in the form of circular buffer.
 */
public class CustomLogProcessor implements CrashReportProcessor {

  private static final CustomLogProcessor INSTANCE = new CustomLogProcessor();
  private final String TAG = "CustomLogProcessor";
  private final int MAX_SIZE = 64;
  private Deque<String> logList = new ArrayDeque<>();

  public static CustomLogProcessor getInstance() {
    return INSTANCE;
  }

  @Override
  public void processTask(Context context, CrashReportTask task) {
  }

  /**
   * Adds a new custom log to the log list.
   */
  public synchronized void addCustomLog(String message) throws LogEntrySizeLimitExceededError {

    if (message == null) {
      Log.e(TAG, "Error passing a null message to Crash Reporting log.");
      return;
    }

    // Prevent storing logs over 1KB.
    if (message.getBytes().length >= 1000) {
      throw new LogEntrySizeLimitExceededError();
    }

    // Removes the oldest log to create space for new custom logs.
    if (logList.size() == MAX_SIZE) {
      removeFirstCustomLog();
    }

    logList.add(message);
  }

  /**
   * Default access for sessionsLifecycleProcessor to retrieve all custom logs.
   */
  @Nullable
  String getCustomLogs() {
    // Returns empty string for no custom logs.
    if (logList.size() == 0) {
      return "";
    }

    // Concatenate all logs as a single string but differentiates them with a new line.
    StringBuilder stringBuilder = new StringBuilder();
    for (String e : logList) {
      stringBuilder.append(e).append("\r\n");
    }

    return stringBuilder.toString();
  }

  /**
   * Removes the oldest log from the list of logs.
   */
  private void removeFirstCustomLog() {
    if (logList.size() != 0) {
      logList.removeFirst();
    }
  }

  private CustomLogProcessor() {
  }
}
