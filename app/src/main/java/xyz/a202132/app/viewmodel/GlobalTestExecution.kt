package xyz.a202132.app.viewmodel

import kotlinx.coroutines.sync.Mutex

object GlobalTestExecution {
    val mutex = Mutex()
    @Volatile
    private var currentTestLabel: String? = null
    private var fetchingDepth: Int = 0
    private var currentFetchingLabel: String? = null

    fun tryStart(testLabel: String): Boolean {
        if (!mutex.tryLock()) return false
        currentTestLabel = testLabel
        return true
    }

    fun finish() {
        currentTestLabel = null
        mutex.unlock()
    }

    fun busyHint(): String {
        val label = currentTestLabel
        return if (label.isNullOrBlank()) {
            "已有测试进行中，请稍后"
        } else {
            "正在进行$label，请稍后"
        }
    }

    fun beginFetching(fetchingLabel: String = "请求节点中") {
        synchronized(this) {
            fetchingDepth += 1
            if (currentFetchingLabel.isNullOrBlank()) {
                currentFetchingLabel = fetchingLabel
            }
        }
    }

    fun endFetching() {
        synchronized(this) {
            if (fetchingDepth > 0) {
                fetchingDepth -= 1
            }
            if (fetchingDepth == 0) {
                currentFetchingLabel = null
            }
        }
    }

    fun isFetching(): Boolean = synchronized(this) { fetchingDepth > 0 }

    fun fetchingHint(): String {
        val label = synchronized(this) { currentFetchingLabel }
        return if (label.isNullOrBlank()) {
            "请求节点中，请稍后"
        } else {
            "$label，请稍后"
        }
    }
}
