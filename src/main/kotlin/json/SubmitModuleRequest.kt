package json

import kotlinx.serialization.Serializable

@Serializable
data class SubmitModuleRequest(val iceKeyAsString: String)