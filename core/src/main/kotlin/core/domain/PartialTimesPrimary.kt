package core.domain

import com.fasterxml.jackson.annotation.JsonProperty
import core.util.GetPropNames.Companion.getPropNames

data class PartialTimesPrimary(
    @get:JsonProperty("dependencies_update")
    val dependenciesUpdate: String,
    @get:JsonProperty("dependencies_install")
    val dependenciesInstall: String,
    @get:JsonProperty("nfs_server_install_and_config")
    val nfsServerInstallAndConfig: String,
    @get:JsonProperty("mpi_download")
    val mpiDownload: String,
    @get:JsonProperty("mpi_config_compile")
    val mpiConfigCompile: String,
) {
    override fun toString() = this::class.getPropNames()
}
