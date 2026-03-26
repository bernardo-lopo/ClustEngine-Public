package core.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URI

const val TOKEN_HEADER_PROPERTY_NAME = "X-Auth-Token"

class FetchData {
    companion object {
        suspend fun get(
            url: String,
            token: String?,
        ): FetchResponse = request("GET", url, token = token)

        suspend fun post(
            url: String,
            body: String,
            contentType: String = "application/json",
            token: String? = null,
        ): FetchResponse = request("POST", url, body, contentType, token)

        suspend fun delete(
            url: String,
            token: String? = null,
        ): FetchResponse = request("DELETE", url, token = token)

        private suspend fun request(
            method: String,
            url: String,
            body: String? = null,
            contentType: String? = null,
            token: String?,
        ): FetchResponse =
            withContext(Dispatchers.IO) {
                val connection = URI(url).toURL().openConnection() as HttpURLConnection

                try {
                    // Sets the HTTP method
                    connection.requestMethod = method

                    // If there is any authentication needed the token is set in the header property
                    if (token != null) connection.setRequestProperty(TOKEN_HEADER_PROPERTY_NAME, token)

                    // Only for post/put methods
                    if (body != null) {
                        connection.doOutput = true
                        contentType?.let { connection.setRequestProperty("Content-Type", it) }

                        // Writes the request body
                        connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                    }

                    // Gets the HTTP status code
                    val status = connection.responseCode

                    val stream = if (status in 200..299) connection.inputStream else connection.errorStream

                    val responseBody = stream.bufferedReader().use { it.readText() }

                    // The header is stored for future uses
                    val header = connection.headerFields

                    FetchResponse(status, responseBody, header)
                } finally {
                    connection.disconnect()
                }
            }
    }
}
