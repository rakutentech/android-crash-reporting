package com.rakuten.tech.mobile.crash;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.rakuten.tech.mobile.crash.tasks.BackgroundTask;
import com.rakuten.tech.mobile.crash.tasks.CrashReportTask;
import com.rakuten.tech.mobile.crash.tasks.FlushLifecyclesTask;
import com.rakuten.tech.mobile.crash.tasks.ForegroundTask;
import com.rakuten.tech.mobile.crash.tasks.NewInstallTask;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * Unit tests for CrashReportTaskQueue.java.
 */
@RunWith(MockitoJUnitRunner.class)
public class CrashReportTaskQueueTest {

  @Mock CrashReportTask mockTask;
  @Mock BackgroundTask mockBackground;
  @Mock ForegroundTask mockForeground;
  @Mock NewInstallTask mockNewInstall;
  @Mock FlushLifecyclesTask mockFlushLifecycles;

  private CrashReportTaskQueue crashReportTaskQueue;

  @Before
  public void setup() {
    crashReportTaskQueue = CrashReportTaskQueue.getInstance();
  }

  /**
   * Test if queue will enqueue and dequeue CrashReportTasks.
   */
  @Test
  public void testEnqueueCrashReportTask() {
    assertTrue(crashReportTaskQueue.enqueue(mockTask));
  }

  @Test
  public void testDequeueCrashReportTask() {
    crashReportTaskQueue.enqueue(mockTask);
    assertEquals(crashReportTaskQueue.dequeue(), mockTask);
  }

  /**
   * Test queue and dequeue for all implementations of CrashReportTask.
   */
  @Test
  public void testQueueOnForegroundTask() {
    crashReportTaskQueue.enqueue(mockForeground);
    assertEquals(crashReportTaskQueue.dequeue() instanceof ForegroundTask, true);
  }

  @Test
  public void testQueueOnBackgroundTask() {
    crashReportTaskQueue.enqueue(mockBackground);
    assertEquals(crashReportTaskQueue.dequeue() instanceof BackgroundTask, true);
  }

  @Test
  public void testQueueOnNewInstallTask() {
    crashReportTaskQueue.enqueue(mockNewInstall);
    assertEquals(crashReportTaskQueue.dequeue() instanceof NewInstallTask, true);
  }

  @Test
  public void testQueueOnFlushLifecyclesTask() {
    crashReportTaskQueue.enqueue(mockFlushLifecycles);
    assertEquals(crashReportTaskQueue.dequeue() instanceof FlushLifecyclesTask, true);
  }

  @Test
  public void testEnqueueDequeueInOrder() {
    crashReportTaskQueue.enqueue(mockFlushLifecycles);
    crashReportTaskQueue.enqueue(mockForeground);
    crashReportTaskQueue.enqueue(mockBackground);
    crashReportTaskQueue.enqueue(mockNewInstall);
    assertEquals(crashReportTaskQueue.dequeue() instanceof FlushLifecyclesTask, true);
    assertEquals(crashReportTaskQueue.dequeue() instanceof ForegroundTask, true);
    assertEquals(crashReportTaskQueue.dequeue() instanceof BackgroundTask, true);
    assertEquals(crashReportTaskQueue.dequeue() instanceof NewInstallTask, true);
  }

  @Test
  public void testEnqueueDequeueOutOfOrder() {
    crashReportTaskQueue.enqueue(mockFlushLifecycles);
    crashReportTaskQueue.enqueue(mockForeground);
    crashReportTaskQueue.enqueue(mockBackground);
    crashReportTaskQueue.enqueue(mockNewInstall);
    assertEquals(crashReportTaskQueue.dequeue() instanceof ForegroundTask, false);
    assertEquals(crashReportTaskQueue.dequeue() instanceof FlushLifecyclesTask, false);
    assertEquals(crashReportTaskQueue.dequeue() instanceof NewInstallTask, false);
    assertEquals(crashReportTaskQueue.dequeue() instanceof BackgroundTask, false);
  }
}
