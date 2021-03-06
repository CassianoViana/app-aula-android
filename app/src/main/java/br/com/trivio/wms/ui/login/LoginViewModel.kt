package br.com.trivio.wms.ui.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.LoginRepository
import br.com.trivio.wms.data.Result

import br.com.trivio.wms.R
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.getErrorMessageCode
import br.com.trivio.wms.globalData
import br.com.trivio.wms.loadUserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(private val loginRepository: LoginRepository) : ViewModel() {

  private val _loginForm = MutableLiveData<LoginFormState>()
  val loginFormState: LiveData<LoginFormState> = _loginForm

  private val _loginResult = MutableLiveData<LoginResult>()
  val loginResult: LiveData<LoginResult> = _loginResult

  fun login(username: String, password: String) {
    globalData.token = ""
    viewModelScope.launch {
      val result: Result<UserDetails> = withContext(Dispatchers.IO) {
        loginRepository.login(username, password)
      }
      if (result is Result.Success) {
        _loginResult.value = LoginResult(success = LoggedInUserView(displayName = result.data.name))
      } else {
        if (result is Result.Error) {
          val errorMessageCode = getErrorMessageCode("login", result.throwable)
          _loginResult.value = LoginResult(error = errorMessageCode)
        }
      }
    }
  }

  fun loginDataChanged(username: String, password: String) {
    if (!isUserNameValid(username)) {
      _loginForm.value = LoginFormState(usernameError = R.string.invalid_username)
    } else if (!isPasswordValid(password)) {
      _loginForm.value = LoginFormState(passwordError = R.string.invalid_password)
    } else {
      _loginForm.value = LoginFormState(isDataValid = true)
    }
  }

  // A placeholder username validation check
  private fun isUserNameValid(username: String): Boolean {
    return if (username.contains('@')) {
      Patterns.EMAIL_ADDRESS.matcher(username).matches()
    } else {
      username.isNotBlank()
    }
  }

  // A placeholder password validation check
  private fun isPasswordValid(password: String): Boolean {
    return password.length >= 3
  }
}
