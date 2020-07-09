package br.com.trivio.wms.data.model

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class UserDetails(
  val id: Long = 0,
  val ownerId: Long = 0,
  val name: String = "",
  val username: String = "",
  val roleDescription: String = "",
  val role: String = ""
) {
  fun isInvalid() = this.id == 0L
}
