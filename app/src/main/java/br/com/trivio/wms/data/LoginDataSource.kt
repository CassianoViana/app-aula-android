package br.com.trivio.wms.data

import br.com.trivio.wms.api
import br.com.trivio.wms.api.UsernamePassword
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.globalData
import br.com.trivio.wms.loadUserDetails
import java.io.IOException
import java.lang.IllegalStateException

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
    return api.login(usernamePassword)
  }

  fun logout() {
    // TODO: revoke authentication
  }
}

