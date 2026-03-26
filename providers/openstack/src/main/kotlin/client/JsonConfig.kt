package client

import kotlinx.serialization.json.Json

object JsonConfig {
    val json =
        Json {
            ignoreUnknownKeys = true
        }
}
