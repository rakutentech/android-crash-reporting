package com.rakuten.tech.mobile.crash.processors;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.rakuten.tech.mobile.crash.exception.LogEntrySizeLimitExceededError;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Processor for adding in-memory arbitrary log messages in the form of circular buffer.
 */
public class CustomLogger {

  private static final CustomLogger INSTANCE = new CustomLogger();
  private final String TAG = "CustomLogger";
  private final int MAX_SIZE = 64;
  private Deque<String> logList = new ArrayDeque<>();


  @VisibleForTesting
  CustomLogger() {}

  @NonNull
  public static CustomLogger getInstance() {
    return INSTANCE;
  }

  /**
   * Adds a new custom log to the log list.
   */
  public synchronized void addCustomLog(@Nullable String message) throws LogEntrySizeLimitExceededError {

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
  @NonNull
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
}
