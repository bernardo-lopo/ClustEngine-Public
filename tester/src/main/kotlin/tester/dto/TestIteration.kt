package tester.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TestIteration(
    val iteration: Int,
    @get:JsonProperty("total_iterations")
    val totalIterations: Int,
) {
    override fun toString(): String {
        return "Test Iteration: $iteration/$totalIterations"
    }
}
