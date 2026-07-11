package core.util

import java.io.File

class HostFileManager(instanceId: String = "") {
    private val tempDirName = "tmp"
    private val hostFileName = if (instanceId.isEmpty()) "tmp_hostfile" else "tmp_hostfile_$instanceId"

    private val projectRoot = File(System.getProperty("user.dir"))
    private val tempDir = File(projectRoot, tempDirName)
    private val hostFile = File(tempDir, hostFileName)

    fun createHostFile(): String {
        if (tempDir.exists()) {
            if (!tempDir.isDirectory) {
                throw IllegalStateException(
                    "The path '${tempDir.absolutePath}' exists but its not a directory.",
                )
            }
        } else {
            val created = tempDir.mkdirs()

            if (!created) {
                throw IllegalStateException(
                    "It was not possible to create the directory named $tempDirName: ${tempDir.absolutePath}",
                )
            }
        }

        if (hostFile.exists()) {
            hostFile.delete()
        }

        hostFile.createNewFile()

        require(hostFile.canWrite()) {
            "$hostFileName is not writable: ${hostFile.absolutePath}"
        }
        return hostFile.absolutePath
    }

    fun deleteHostFile() {
        if (hostFile.exists()) {
            hostFile.delete()
        }
    }
}
