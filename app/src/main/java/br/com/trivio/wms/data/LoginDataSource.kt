package br.com.trivio.wms.data

import br.com.trivio.wms.api.UsernamePassword
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.globalData
import br.com.trivio.wms.loadUserDetails
import br.com.trivio.wms.serverBackend
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

  suspend fun login(username: String, password: String): Result<UserDetails> {
    return try {
      val usernamePassword = UsernamePassword(username, password)
      val token = fetchToken(usernamePassword)
      globalData.token = token
      when (token) {
        null -> Result.Error(IllegalStateException("Cannot load user. Token is null"))
        else -> Result.Success(loadUserDetails())
      }
    } catch (e: Throwable) {
      e.printStackTrace()
      Result.Error(IOException("Error logging in", e))
    }
  }

  private fun fetchToken(usernamePassword: UsernamePassword): String? {
    return serverBackend.login(usernamePassword)
  }

  fun logout() {
    // TODO: revoke authentication
  }
}

