package core.domain

data class ClustEngineInstance(
    val id: String,
    val privateIpAddress: String,
    val publicDns: String,
    val publicIp: String,
)
