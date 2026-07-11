package core.util

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test

class SshCommandBuilderTest {
    @Test
    fun `should build SSH command to check connection (commandToCheck)`() {
        val command =
            SshCommandBuilder.buildSshCommand(
                host = "1.2.3.4",
                user = "ubuntu",
                identityFile = "/keys/cluster.pem",
                knownHostsFile = "tmp/tmp_hostfile_1",
                extraOptions = listOf("-o", "ExitOnForwardFailure=yes", "-o", "StrictHostKeyChecking=no"),
                remoteCommand = "exit",
            )

        val expected =
            arrayOf(
                "ssh",
                "-o", "ExitOnForwardFailure=yes",
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=tmp/tmp_hostfile_1",
                "-i", "/keys/cluster.pem",
                "ubuntu@1.2.3.4",
                "exit",
            )

        assertArrayEquals(expected, command)
    }

    @Test
    fun `should build SSH command with port forwarding and pseudo-terminal (baseCommandSsh)`() {
        val command =
            SshCommandBuilder.buildSshCommand(
                host = "1.2.3.4",
                user = "ubuntu",
                identityFile = "/keys/cluster.pem",
                knownHostsFile = "tmp/tmp_hostfile_1",
                forcePseudoTerminal = true,
                extraOptions = listOf("-o", "ExitOnForwardFailure=yes", "-o", "LogLevel=ERROR", "-o", "StrictHostKeyChecking=no"),
                portForwarding = listOf("-L", "2222:10.0.0.5:22"),
            )

        val expected =
            arrayOf(
                "ssh",
                "-t", "-t",
                "-o", "ExitOnForwardFailure=yes",
                "-o", "LogLevel=ERROR",
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=tmp/tmp_hostfile_1",
                "-i", "/keys/cluster.pem",
                "-L", "2222:10.0.0.5:22",
                "ubuntu@1.2.3.4",
            )

        assertArrayEquals(expected, command)
    }

    @Test
    fun `should build SCP command without port (runScriptOnInstance)`() {
        val command =
            SshCommandBuilder.buildScpCommand(
                sourcePath = "scripts/setup_cluster.sh",
                destinationPath = "ubuntu@1.2.3.4:",
                identityFile = "/keys/cluster.pem",
                knownHostsFile = "tmp/tmp_hostfile",
                extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
            )

        val expected =
            arrayOf(
                "scp",
                "-o", "StrictHostKeyChecking=no",
                "-o", "ConnectTimeout=10",
                "-o", "UserKnownHostsFile=tmp/tmp_hostfile",
                "-i", "/keys/cluster.pem",
                "scripts/setup_cluster.sh",
                "ubuntu@1.2.3.4:",
            )

        assertArrayEquals(expected, command)
    }

    @Test
    fun `should build SCP command with custom port (runScriptOnInstanceUsingOnePublicIp)`() {
        val command =
            SshCommandBuilder.buildScpCommand(
                sourcePath = "scripts/setup_cluster.sh",
                destinationPath = "ubuntu@localhost:",
                identityFile = "/keys/cluster.pem",
                knownHostsFile = "tmp/tmp_hostfile_2222",
                port = 2222,
                extraOptions = listOf("-o", "StrictHostKeyChecking=no"),
            )

        val expected =
            arrayOf(
                "scp",
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=tmp/tmp_hostfile_2222",
                "-i", "/keys/cluster.pem",
                "-P", "2222",
                "scripts/setup_cluster.sh",
                "ubuntu@localhost:",
            )

        assertArrayEquals(expected, command)
    }

    @Test
    fun `should build SSH command to execute script remotely with arguments (allowExecutionAndRun)`() {
        val remoteScriptPath = "/home/ubuntu/setup_cluster.sh"
        val remoteExecution = "chmod +x $remoteScriptPath; $remoteScriptPath -p -s 10.0.0.1 10.0.0.2"

        val command =
            SshCommandBuilder.buildSshCommand(
                host = "localhost",
                user = "ubuntu",
                identityFile = "/keys/cluster.pem",
                knownHostsFile = "tmp/tmp_hostfile_2222",
                port = 2222,
                extraOptions = listOf("-o", "StrictHostKeyChecking=no", "-o", "ConnectTimeout=10"),
                remoteCommand = remoteExecution,
            )

        val expected =
            arrayOf(
                "ssh",
                "-o", "StrictHostKeyChecking=no",
                "-o", "ConnectTimeout=10",
                "-o", "UserKnownHostsFile=tmp/tmp_hostfile_2222",
                "-i", "/keys/cluster.pem",
                "-p", "2222",
                "ubuntu@localhost",
                "chmod +x /home/ubuntu/setup_cluster.sh; /home/ubuntu/setup_cluster.sh -p -s 10.0.0.1 10.0.0.2",
            )

        assertArrayEquals(expected, command)
    }
}
