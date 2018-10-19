package com.rakuten.tech.mobile.crash.processors;

import android.content.Context;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;

/**
 * Interface for processors performing tasks within Crash Report SDK.
 */
public interface CrashReportProcessor {

  void processTask(Context context, CrashReportTask task);
}
