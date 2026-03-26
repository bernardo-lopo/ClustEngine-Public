package core.util

import org.junit.jupiter.api.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TimeMeasureTest {
    @Test
    fun `timeElapsed should measure execution duration and return formatted string`() {
        val elapsedString =
            TimeMeasure.timeElapsed {
                Thread.sleep(1100)
            }

        // The result should be in the format like: "1s -> 0m 1s"
        // It is checked if it contains "1s" or more
        val secondsValue = elapsedString.substringBefore("s").toIntOrNull()

        // Ensures the time measured is at least 1 second
        assertTrue(secondsValue != null && secondsValue >= 1, "Expected time to be >= 1s")
    }

    /*
     * Validates that the output format of timeElapsed matches the expected regex pattern:
     * e.g, "65s -> 1m 5s"
     */
    @Test
    fun `timeElapsed output should match expected regex format`() {
        val elapsed =
            TimeMeasure.timeElapsed {
                Thread.sleep(100)
            }

        // Regex explanation:
        // - ^        : start of string
        // - \\d+     : one or more digits (e.g., total seconds)
        // - s ->     : literal "s ->"
        // - \\s*     : optional whitespace
        // - \\d+     : minutes
        // - m        : literal "m"
        // - \\s+     : at least one space
        // - \\d+     : seconds
        // - s$       : literal "s" at the end of string
        val regex = Regex("^\\d+s ->\\s*\\d+m\\s+\\d+s$")

        assertTrue(regex.matches(elapsed), "Output does not match expected time format")

        val invalid = "12s=>0m 12s"
        assertFalse(regex.matches(invalid), "Invalid string '$invalid' should not match the expected format")

        val valid = "75s -> 1m 15s"
        assertTrue(regex.matches(valid), "Expected format '$valid' to match regex")
    }
}
