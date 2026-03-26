package tester.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class TestedCluster(
    val iteration: String,
    @get:JsonProperty("total_time")
    val totalTime: String,
    @get:JsonProperty("partial_times")
    val partialTimes: PartialTimesOutput,
    @get:JsonProperty("number_of_instances")
    val numberOfInstances: Int,
    @get:JsonProperty("tested_at")
    val testedAt: String,
)
