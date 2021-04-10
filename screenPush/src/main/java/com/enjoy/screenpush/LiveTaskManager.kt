package com.enjoy.screenpush

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

/**
 * 线程池任务管理类LiveTaskManager。

 * 为什么使用线程池：

当需要处理的任务较少时，我们可以自己创建线程去处理，但在高并发场景下，我们需要处理的任务数量很多，由于创建销毁线程开销很大，这样频繁创建线程就会大大降低系统的效率。

此时，我们就可以使用线程池，线程池中的线程执行完一个任务后可以复用，并不被销毁。

合理使用线程池有以下几点好处：

1、减少资源的开销。通过复用线程，降低创建销毁线程造成的消耗。

2、多个线程并发执行任务，提高系统的响应速度。

3、可以统一的分配，调优和监控线程，提高线程的可管理性。
 */
class LiveTaskManager {

    private var threadPoolExecutor: ThreadPoolExecutor? = null

    /**
     * 初始化线程池
     */
    init {
        var threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS.toLong(), TimeUnit.SECONDS,
                sPoolWorkQueue)
        threadPoolExecutor.allowCoreThreadTimeOut(true)
        this.threadPoolExecutor = threadPoolExecutor
    }

    /**
     * 向线程池提交任务 execute or submit
     */
    open fun execute(runnable: Runnable) {
        threadPoolExecutor?.let { threadPoolExecutor ->
            threadPoolExecutor.execute(runnable)
        }
    }

    companion object {
        private val CPU_COUNT = Runtime.getRuntime().availableProcessors()
        private val CORE_POOL_SIZE = Math.max(2, Math.min(CPU_COUNT - 1, 4))
        private val MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1
        private val KEEP_ALIVE_SECONDS = 30
        private val sPoolWorkQueue = LinkedBlockingQueue<Runnable>(5)

        @Volatile
        private var instance: LiveTaskManager? = null

        open fun getInstance(): LiveTaskManager {
            if (instance == null) {
                synchronized(LiveTaskManager::class.java) {
                    if (instance == null) {
                        instance = LiveTaskManager()
                    }
                }
            }
            return instance!!
        }
    }
}
