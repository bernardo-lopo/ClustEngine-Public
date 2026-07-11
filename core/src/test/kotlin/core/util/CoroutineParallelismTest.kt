package core.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap

class CoroutineParallelismTest {
    @Test
    fun `should use more than 64 unique threads for concurrent IO tasks`() {
        val numberOfTasks = 100

        val uniqueThreads = ConcurrentHashMap.newKeySet<String>()

        runBlocking {
            val jobs =
                List(numberOfTasks) {
                    launch(Dispatchers.IO) {
                        uniqueThreads.add(Thread.currentThread().name)
                    /*
                        Here it is used the Thread.sleep because delay will reals the thread,
                        on the other hand Thread.sleep() will block it,
                        forcing the dispatcher to allocate a dedicated thread for each task.
                     */
                        Thread.sleep(500)
                    }
                }

            jobs.joinAll()
        }

        /*
            If the pool is limited to 64, it can never use more than 64 unique threads for this pool,
            regardless of how many tasks are queued. If the limit is 512, it should use exactly 100.
         */
        assertTrue(
            uniqueThreads.size > 64,
            "Expected more than 64 unique threads, but found ${uniqueThreads.size}",
        )
    }
}
