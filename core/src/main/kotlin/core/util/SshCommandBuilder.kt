package core.util

object SshCommandBuilder {
    fun buildSshCommand(
        host: String,
        user: String = "ubuntu",
        identityFile: String,
        knownHostsFile: String? = null,
        port: Int? = null,
        forcePseudoTerminal: Boolean = false,
        extraOptions: List<String> = emptyList(),
        portForwarding: List<String> = emptyList(),
        remoteCommand: String? = null,
    ): Array<String> =
        buildList {
            add("ssh")

            if (forcePseudoTerminal) {
                add("-t")
                add("-t")
            }

            addAll(extraOptions)

            if (knownHostsFile != null) {
                add("-o")
                add("UserKnownHostsFile=$knownHostsFile")
            }

            add("-i")
            add(identityFile)

            addAll(portForwarding)

            if (port != null) {
                add("-p")
                add(port.toString())
            }

            add("$user@$host")

            if (remoteCommand != null) {
                add(remoteCommand)
            }
        }.toTypedArray()

    fun buildScpCommand(
        sourcePath: String,
        destinationPath: String,
        identityFile: String,
        knownHostsFile: String? = null,
        port: Int? = null,
        extraOptions: List<String> = emptyList(),
    ): Array<String> =
        buildList {
            add("scp")

            addAll(extraOptions)

            if (knownHostsFile != null) {
                add("-o")
                add("UserKnownHostsFile=$knownHostsFile")
            }

            add("-i")
            add(identityFile)

            if (port != null) {
                add("-P")
                add(port.toString())
            }

            add(sourcePath)
            add(destinationPath)
        }.toTypedArray()
}
