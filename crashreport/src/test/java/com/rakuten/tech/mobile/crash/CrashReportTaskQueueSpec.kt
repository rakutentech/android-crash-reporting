package com.rakuten.tech.mobile.crash

import com.rakuten.tech.mobile.crash.tasks.BackgroundTask
import com.rakuten.tech.mobile.crash.tasks.FlushLifecyclesTask
import com.rakuten.tech.mobile.crash.tasks.ForegroundTask
import com.rakuten.tech.mobile.crash.tasks.NewInstallTask
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeTrue
import org.amshove.kluent.shouldEqual
import org.junit.Test

class CrashReportTaskQueueSpec {

    private val backgroundTask = BackgroundTask(0)
    private val foregroundTask = ForegroundTask(0)
    private val installTask = NewInstallTask()
    private val flushTask = FlushLifecyclesTask()

    private val queue = CrashReportTaskQueue()

    /**
     * Test if queue will enqueue and dequeue CrashReportTasks.
     */
    @Test
    fun testEnqueueCrashReportTask() {
        queue.enqueue(backgroundTask).shouldBeTrue()
    }

    @Test
    fun testDequeueCrashReportTask() {
        queue.enqueue(backgroundTask)

        queue.dequeue().shouldEqual(backgroundTask)
    }

    /**
     * Test queue and dequeue for all implementations of CrashReportTask.
     */
    @Test
    fun testQueueOnForegroundTask() {
        queue.enqueue(foregroundTask)

        queue.dequeue().shouldBeInstanceOf(ForegroundTask::class)
    }

    @Test
    fun testQueueOnBackgroundTask() {
        queue.enqueue(backgroundTask)

        queue.dequeue().shouldBeInstanceOf(BackgroundTask::class)
    }

    @Test
    fun testQueueOnNewInstallTask() {
        queue.enqueue(installTask)

        queue.dequeue().shouldBeInstanceOf(NewInstallTask::class)
    }

    @Test
    fun testQueueOnFlushLifecyclesTask() {
        queue.enqueue(flushTask)

        queue.dequeue().shouldBeInstanceOf(FlushLifecyclesTask::class)
    }

    @Test
    fun testEnqueueDequeueInOrder() {
        queue.enqueue(flushTask)
        queue.enqueue(foregroundTask)
        queue.enqueue(backgroundTask)
        queue.enqueue(installTask)

        queue.dequeue().shouldBeInstanceOf(FlushLifecyclesTask::class)
        queue.dequeue().shouldBeInstanceOf(ForegroundTask::class)
        queue.dequeue().shouldBeInstanceOf(BackgroundTask::class)
        queue.dequeue().shouldBeInstanceOf(NewInstallTask::class)
    }

    @Test
    fun testEnqueueDequeueOutOfOrder() {
        queue.enqueue(flushTask)
        queue.enqueue(foregroundTask)
        queue.enqueue(backgroundTask)
        queue.enqueue(installTask)

        queue.dequeue().shouldBeInstanceOf(FlushLifecyclesTask::class)
        queue.dequeue().shouldBeInstanceOf(ForegroundTask::class)
        queue.dequeue().shouldBeInstanceOf(BackgroundTask::class)
        queue.dequeue().shouldBeInstanceOf(NewInstallTask::class)
    }
}
