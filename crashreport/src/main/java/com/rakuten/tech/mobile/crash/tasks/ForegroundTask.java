package com.rakuten.tech.mobile.crash.tasks;

/**
 * A representation of an event when application went into foreground.
 */
public class ForegroundTask implements CrashReportTask {

  private final long timestamp;

  public ForegroundTask(Long time) {
    timestamp = time;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public TaskType getType() {
    return TaskType.FOREGROUND;
  }
}
