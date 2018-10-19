package com.rakuten.tech.mobile.crash.tasks;

/**
 * Representation of a trigger to send cached lifecycles to Crash Report server.
 */
public class FlushLifecyclesTask implements CrashReportTask {

  public TaskType getType() {
    return TaskType.FLUSH_LIFECYCLES;
  }
}
