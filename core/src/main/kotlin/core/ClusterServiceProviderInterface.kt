package core

import core.domain.PartialTimesNonPrimary
import core.domain.PartialTimesPrimary

interface ClusterServiceProviderInterface {
    fun isClusterInited(): Boolean

    fun startAll()

    fun stopAll()

    fun deleteCluster()

    fun startById(id: String)

    fun stopById(id: String)

    fun deleteInstance(id: String)

    fun init(): Pair<PartialTimesPrimary, List<PartialTimesNonPrimary>>
}
