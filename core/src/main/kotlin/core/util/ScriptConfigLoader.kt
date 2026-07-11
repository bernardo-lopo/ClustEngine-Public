package core.util

import java.io.File

object ScriptConfigLoader {
    private val mutableDotEnvVars = mutableMapOf<String, String>()

    init {
        reloadEnv()
    }

    fun reloadEnv() {
        val envFile = File(".env").takeIf { it.exists() } ?: File("../.env")
        if (envFile.exists()) {
            val parsed =
                envFile.readLines()
                    .filter { it.isNotBlank() && !it.startsWith("#") && it.contains("=") }
                    .associate { line ->
                        val (key, value) = line.split("=", limit = 2)
                        val cleanKey = key.trim().removePrefix("export").trim()
                        cleanKey to value.trim().removeSurrounding("\"").removeSurrounding("'")
                    }
            mutableDotEnvVars.clear()
            mutableDotEnvVars.putAll(parsed)
        }
    }

    fun getEnvOrNull(name: String): String {
        return mutableDotEnvVars[name] ?: System.getenv(name) ?: ""
    }

    fun getEnvOrThrow(name: String): String {
        val value = getEnvOrNull(name)
        if (value.isBlank()) throw IllegalArgumentException("Error: Env variable '$name' is missing")
        return value
    }

    fun saveEnvVariables(newVars: Map<String, String>) {
        val envFile = File(".env")
        mutableDotEnvVars.putAll(newVars)
        val content = mutableDotEnvVars.map { "${it.key}=\"${it.value}\"" }.joinToString("\n")
        envFile.writeText(content)
    }

    val SCRIPT_PATH: String get() = getEnvOrThrow("BASE_SCRIPT_PATH")
    val SCRIPT_NAME: String get() = getEnvOrThrow("BASE_SCRIPT_NAME")
    val USER_SCRIPT_PATH: String get() = getEnvOrThrow("USER_SCRIPT_PATH")
}
