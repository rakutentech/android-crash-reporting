package com.rakuten.tech.mobile.crash.tasks;

/**
 * Interface for all asynchronous tasks performed by the Crash Report SDK.
 */
public interface CrashReportTask {

  TaskType getType();

  enum TaskType {
    BACKGROUND,
    FLUSH_LIFECYCLES,
    FOREGROUND,
    GET_CONFIG,
    NEW_INSTALL
  }
}
