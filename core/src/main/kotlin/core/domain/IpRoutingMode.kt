package core.domain

sealed class IpRoutingMode

class SingleIpRouting : IpRoutingMode()

class MultiIpRouting : IpRoutingMode()
