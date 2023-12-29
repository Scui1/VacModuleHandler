package json

import kotlinx.serialization.Serializable

@Serializable
data class SubmitModuleResponse(val success: Boolean, val message: String)
