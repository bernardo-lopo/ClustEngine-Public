package core

enum class ErrorCategory {
    INFRASTRUCTURE_FAILURE,
    RESOURCE_EXHAUSTED,
    NETWORK_TIMEOUT,
    VALIDATION,
    SYSTEM_INTERNAL,
}

sealed class ClustEngineProblem()
