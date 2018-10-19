package com.rakuten.tech.mobile.crash.tasks;

import com.rakuten.tech.mobile.crash.processors.ConfigProcessor.OnConfigSuccessCallback;

/**
 * Representation of a trigger to request SDK settings from the configuration server.
 */
public class GetConfigTask implements CrashReportTask {

  private final OnConfigSuccessCallback callback;

  public GetConfigTask(OnConfigSuccessCallback callback) {
    this.callback = callback;
  }

  public OnConfigSuccessCallback getCallback() {
    return callback;
  }

  @Override
  public TaskType getType() {
    return TaskType.GET_CONFIG;
  }
}
