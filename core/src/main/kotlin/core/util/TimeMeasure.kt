package core.util

class TimeMeasure() {
    companion object {
        fun timeElapsed(code: () -> Unit): String {
            val startTime = System.nanoTime()
            code.invoke()
            val elapsedTimeInSeconds = (System.nanoTime() - startTime) / 1_000_000_000

            val minutes = elapsedTimeInSeconds / 60
            val seconds = elapsedTimeInSeconds % 60

            return "${elapsedTimeInSeconds}s -> ${minutes}m ${seconds}s"
        }
    }
}
