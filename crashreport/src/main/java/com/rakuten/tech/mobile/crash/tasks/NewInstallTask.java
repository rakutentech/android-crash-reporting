package com.rakuten.tech.mobile.crash.tasks;

/**
 * Representation of a trigger to attempt possible (but not always guaranteed) report of a new SDK install event to Crash Report server.
 */
public class NewInstallTask implements CrashReportTask {

  public TaskType getType() {
    return TaskType.NEW_INSTALL;
  }
}
