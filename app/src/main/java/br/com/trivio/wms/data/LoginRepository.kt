package br.com.trivio.wms.data

import android.content.Context
import br.com.trivio.wms.data.model.UserDetails

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */

class LoginRepository(val dataSource: LoginDataSource) {

  // in-memory cache of the loggedInUser object
  private var userDetails: UserDetails? = null

  val isLoggedIn: Boolean
    get() = userDetails != null

  init {
    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    userDetails = null
  }

  fun logout() {
    userDetails = null
    dataSource.logout()
  }

  suspend fun login(username: String, password: String): Result<UserDetails> {
    val result = dataSource.login(username, password)
    if (result is Result.Success) {
      setLoggedInUser(result.data)
    }
    return result
  }

  private fun setLoggedInUser(userDetails: UserDetails) {
    this.userDetails = userDetails
    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
  }
}
