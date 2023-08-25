package json

import kotlinx.serialization.Serializable

@Serializable
data class DecryptRequest(val iceKeyAsString: String)