package client

class OpenStackSdk(val baseUrl: String) {
    fun getAllIntancesUrl() = "$baseUrl/servers"

    fun createClusterUrl() = "$baseUrl/servers"

    fun getAllIntanceDetailsUrl() = "$baseUrl/servers/detail"

    fun getIntanceDetailsUrl(id: Int) = "$baseUrl/servers/$id"

    fun updateIntanceUrl(id: Int) = "$baseUrl/servers/$id"

    fun deleteIntanceUrl(id: String) = "$baseUrl/servers/$id"

    fun stopIntanceUrl(id: String) = "$baseUrl/servers/$id/action"

    fun startIntanceUrl(id: String) = "$baseUrl/servers/$id/action"

    fun performeActionOnIntance(id: String) = "$baseUrl/servers/$id/action"

    fun getAuthToken() = "https://stratus.d.acnca.pt:5000/v3/auth/tokens"

    // fun getAvailablePublicIP() = "$baseUrl/os-floating-ips"
}
