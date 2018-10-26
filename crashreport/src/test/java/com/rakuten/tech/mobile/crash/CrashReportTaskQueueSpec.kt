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

    @Test
    fun `should confirm enqueue`() {
        queue.enqueue(backgroundTask).shouldBeTrue()
    }

    @Test
    fun `should dequeue previously added task (fifo)`() {
        queue.enqueue(backgroundTask)

        queue.dequeue().shouldEqual(backgroundTask)
    }

    @Test
    fun `should accept ForgroundTask`() {
        queue.enqueue(foregroundTask)

        queue.dequeue().shouldBeInstanceOf(ForegroundTask::class)
    }

    @Test
    fun `should accept BackgroundTask`() {
        queue.enqueue(backgroundTask)

        queue.dequeue().shouldBeInstanceOf(BackgroundTask::class)
    }

    @Test
    fun `should accept NewInstallTask`() {
        queue.enqueue(installTask)

        queue.dequeue().shouldBeInstanceOf(NewInstallTask::class)
    }

    @Test
    fun `should accept FlushLifecycleTask`() {
        queue.enqueue(flushTask)

        queue.dequeue().shouldBeInstanceOf(FlushLifecyclesTask::class)
    }

    @Test
    fun `should dequeue in specific order if enqueued in same order`() {
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
    fun `should dequeue in specific order if enqueued in different order`() {
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