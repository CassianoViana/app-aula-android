package br.com.trivio.wms.data

import br.com.trivio.wms.api.UsernamePassword
import br.com.trivio.wms.api.api
import br.com.trivio.wms.data.model.UserDetails
import java.io.IOException

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
class LoginDataSource {

  fun login(username: String, password: String): Result<UserDetails> {
    return try {
      val usernamePassword = UsernamePassword(username, password)
      val userDetails = fetchUser(usernamePassword)
      Result.Success(userDetails)
    } catch (e: Throwable) {
      e.printStackTrace()
      Result.Error(IOException("Error logging in", e))
    }
  }

  private fun fetchUser(usernamePassword: UsernamePassword): UserDetails {
    val token = api.login(usernamePassword)
    globalData.token = token
    return api.getUserDetails()
  }

  fun logout() {
    // TODO: revoke authentication
  }
}

