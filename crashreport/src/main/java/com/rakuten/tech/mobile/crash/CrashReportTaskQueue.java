package com.rakuten.tech.mobile.crash;

import android.util.Log;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Thread safe queue containing tasks that requires execution in FIFO order.
 */
class CrashReportTaskQueue {

  private static final CrashReportTaskQueue INSTANCE = new CrashReportTaskQueue();
  private final static String TAG = "CrashReportTaskQueue";
  private static final int MAX_SIZE = 50;
  private static Queue<CrashReportTask> queue = new LinkedList<>();

  private CrashReportTaskQueue() {
  }

  static CrashReportTaskQueue getInstance() {
    return INSTANCE;
  }

  /**
   * Add a task into the queue ready to be sent to the crash reporting server.
   */
  synchronized boolean enqueue(CrashReportTask task) {
    try {
      while (queue.size() == MAX_SIZE) {
        wait();
      }
      queue.add(task);
      notifyAll();
      return true;
    } catch (Exception e) {
      Log.e(TAG, "Failed to enqueue task.", e);
    }

    return false;
  }

  /**
   * Remove a task in queue that has been sent over to the crash reporting server.
   */
  synchronized CrashReportTask dequeue() {
    try {
      while (queue.isEmpty()) {
        wait();
      }
      CrashReportTask task = queue.remove();
      notifyAll();
      return task;
    } catch (Exception e) {
      Log.e(TAG, "Failed to dequeue task.", e);
    }

    return null;
  }
}
