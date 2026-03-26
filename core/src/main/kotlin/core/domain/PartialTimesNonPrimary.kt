package core.domain

import com.fasterxml.jackson.annotation.JsonProperty
import core.util.GetPropNames.Companion.getPropNames

data class PartialTimesNonPrimary(
    @get:JsonProperty("dependencies_update")
    val dependenciesUpdate: String,
    @get:JsonProperty("dependencies_install")
    val dependenciesInstall: String,
    @get:JsonProperty("nfs_client_install")
    val nfsClientInstall: String,
) {
    override fun toString() = this::class.getPropNames()
}
