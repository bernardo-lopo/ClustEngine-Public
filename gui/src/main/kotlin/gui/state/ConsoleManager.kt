package gui.state

import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.io.PrintStream
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class ConsoleManager(private val coroutineScope: CoroutineScope) {
    val logs = mutableStateListOf<String>()

    init {
        setupConsoleInterceptor()
    }

    fun log(message: String) {
        val time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"))
        logs.add("[$time] $message")
    }

    fun clear() = logs.clear()

    private fun setupConsoleInterceptor() {
        val originalOut = System.out
        val interceptStream =
            object : OutputStream() {
                private val buffer = StringBuilder()

                override fun write(b: Int) {
                    val c = b.toChar()
                    if (c == '\n') {
                        val msg = buffer.toString()
                        buffer.clear()
                        coroutineScope.launch { log(msg) }
                    } else if (c != '\r') {
                        buffer.append(c)
                    }
                    originalOut.write(b)
                }
            }
        val printStream = PrintStream(interceptStream, true)
        System.setOut(printStream)
        System.setErr(printStream)
    }
}
