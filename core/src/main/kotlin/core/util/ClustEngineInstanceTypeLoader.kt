package core.util

import core.domain.ClustEngineInstanceType
import kotlinx.serialization.json.Json
import java.io.File

object ClustEngineInstanceTypeLoader {
    private fun getAppRootDir(): File {
        val currentDir = File(System.getProperty("user.dir")).absoluteFile
        val submodules = setOf("app", "core", "providers", "tui", "tester", "gui")

        return if (submodules.contains(currentDir.name)) {
            currentDir.parentFile
        } else {
            currentDir
        }
    }

    fun loadClustEngineInstanceType(filePath: String): List<ClustEngineInstanceType> {
        val rootDir = getAppRootDir()
        val file = File(rootDir, filePath)

        if (!file.exists()) {
            println("Warning: Config file ${file.absolutePath} not found.")
            return emptyList()
        }
        return try {
            val content = file.readText()
            Json.decodeFromString<List<ClustEngineInstanceType>>(content)
        } catch (e: Exception) {
            println("Error parsing ${file.absolutePath}: ${e.message}")
            emptyList()
        }
    }
}
