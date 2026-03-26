package core.util

import java.io.FileInputStream
import java.util.Properties

class ConfigurationsLoader(filePath: String) {
    private val configFile = Properties()

    init {
        configFile.load(
            FileInputStream(filePath),
        )
    }

    fun getProperty(key: String): String {
        return configFile.getProperty(key)
    }
}
