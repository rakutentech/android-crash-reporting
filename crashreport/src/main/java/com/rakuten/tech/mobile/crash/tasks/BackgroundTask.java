package com.rakuten.tech.mobile.crash.tasks;

/**
 * A representation of an event when application went into background.
 */
public class BackgroundTask implements CrashReportTask {

  private final long timestamp;

  public BackgroundTask(Long time) {
    timestamp = time;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public TaskType getType() {
    return TaskType.BACKGROUND;
  }
}
