package com.rakuten.tech.mobile.crash;

import android.util.Log;

/**
 * Handle exceptions caused by the SDK and prevents crashing the host app.
 */
class CrashReportingExceptionHandler implements Thread.UncaughtExceptionHandler {

  private final String TAG = "CRExceptionHandler";

  @Override
  public void uncaughtException(Thread thread, Throwable throwable) {
    Log.e(TAG, "Crash Reporting threw an exception while running a process. Please contact the "
            + "Crash Reporting developers about this issue at https://github.com/rakutentech/android-crash-reporting",
        throwable);
  }
}
