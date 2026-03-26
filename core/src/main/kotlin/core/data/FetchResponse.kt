package core.data

data class FetchResponse(val status: Int, val body: String, val header: Map<String, List<String>>)
