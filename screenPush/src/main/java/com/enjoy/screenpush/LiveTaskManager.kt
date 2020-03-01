package com.enjoy.screenpush

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

class LiveTaskManager {

    private var threadPoolExecutor: ThreadPoolExecutor? = null

    init {
        var threadPoolExecutor = ThreadPoolExecutor(
                CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE_SECONDS.toLong(), TimeUnit.SECONDS,
                sPoolWorkQueue)
        threadPoolExecutor.allowCoreThreadTimeOut(true)
        this.threadPoolExecutor = threadPoolExecutor
    }

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
