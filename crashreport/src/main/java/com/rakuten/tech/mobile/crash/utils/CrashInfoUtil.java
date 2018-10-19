package com.rakuten.tech.mobile.crash.utils;

import android.content.Context;
import android.util.Log;
import com.rakuten.tech.mobile.crash.CrashReportConstants;
import java.util.Map;

/**
 * Utility class used to aggregate application crash details.
 */
public class CrashInfoUtil {

  private static final CrashInfoUtil INSTANCE = new CrashInfoUtil();

  public static CrashInfoUtil getInstance() {
    return INSTANCE;
  }

  /**
   * Extracts the package and class name from the stack trace of a given throwable.
   */
  public String getPackageAndClassName(Context context, Throwable ex) {

    // Searches through the throwable stack trace.
    if (ex.getCause() != null) {
      for (StackTraceElement stackTraceElement : ex.getCause().getStackTrace()) {

        // Detects the most recent occurrence of a class belonging to the hosting app in the stack trace of the throwable.
        String lineFromStackTrace = stackTraceElement.toString();
        if (lineFromStackTrace.contains(context.getApplicationInfo().packageName)) {
          return lineFromStackTrace;
        }
      }
    }

    // Returns the closest class name and line number line number where the given throwable occurred.
    return ex.getStackTrace()[0].toString();
  }

  /**
   * Extracts the stack trace of an exception.
   */
  public String getExceptionStackTrace(Throwable ex) {
    // Truncates a string to at most 5000 characters (approximately: 100 characters per 50 lines).
    return Log
        .getStackTraceString(ex)
        .substring(0, Math.min(Log.getStackTraceString(ex).length(), 5000));
  }

  /**
   * Extracts all stack traces at the time of crash.
   */
  public String getStackTracesFromAllThreads() {
    StringBuilder allStackTraces = new StringBuilder();

    for (Map.Entry<Thread, StackTraceElement[]> elem : Thread.getAllStackTraces().entrySet()) {
      if (elem.getValue().length != 0) {
        allStackTraces.append(CrashReportConstants.THREADS_KEY).append(elem.getKey().getName());
        for (StackTraceElement e : elem.getValue()) {
          allStackTraces.append("\n\t").append(e.toString());
        }
        allStackTraces.append("\n");
      }
    }

    return allStackTraces.toString();
  }

  private CrashInfoUtil() {
  }
}