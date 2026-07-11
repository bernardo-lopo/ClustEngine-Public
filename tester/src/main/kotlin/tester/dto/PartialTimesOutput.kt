package tester.dto

import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary

data class PartialTimesOutput(
    val pMachine: PartialTimesPrimary?,
    val nPMachines: List<PartialTimesNonPrimary?>,
) {
    override fun toString(): String {
        return buildString {
            appendLine("Primary Machine:")
            pMachine.toString().lines().forEach { appendLine("  $it") }

            nPMachines.forEachIndexed { index, machine ->
                appendLine("Machine${index + 2}:")
                machine.toString().lines().forEach { appendLine("  $it") }
            }
        }
    }
}
