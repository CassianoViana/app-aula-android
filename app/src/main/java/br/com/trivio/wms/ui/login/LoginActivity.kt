package br.com.trivio.wms.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import br.com.trivio.wms.MainActivity
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.afterTextChanged
import br.com.trivio.wms.extensions.getErrorOrNull
import br.com.trivio.wms.loadApiSettingsFromPreferences

class LoginActivity : MyAppCompatActivity() {

  private lateinit var loginViewModel: LoginViewModel
  private lateinit var usernameInput: EditText
  private lateinit var passwordInput: EditText
  private lateinit var loginButton: Button
  private lateinit var settingsButton: Button
  private lateinit var loadingIndicator: ProgressBar

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)
    bindViews()
    setupViewModelObservers()
    setupTextInputEvents()
    setupButtonsEvents()
  }

  private fun bindViews() {
    usernameInput = findViewById(R.id.username)
    passwordInput = findViewById(R.id.password)
    loginButton = findViewById(R.id.login)
    settingsButton = findViewById(R.id.settings)
    loadingIndicator = findViewById(R.id.loading)
  }

  private fun setupViewModelObservers() {
    val owner = this

    loginViewModel = ViewModelProviders.of(this, ViewModelFactory())
      .get(LoginViewModel::class.java)

    loginViewModel.loginFormState
      .observe(owner, Observer {
        it?.let {
          onLoginFormStateChanged(it)
        }
      })

    loginViewModel.loginResult
      .observe(owner, Observer {
        it?.let {
          onLoginResultChanged(it)
        }
      })
  }

  private fun onLoginResultChanged(loginResult: LoginResult) {
    loadingIndicator.visibility = View.GONE
    if (loginResult.error != null) {
      showLoginFailed(R.string.login_failed)
      return
    }
    showLoginSuccess(loginResult.success)
    setResult(Activity.RESULT_OK)
    startMainActivity()
    finish()
  }

  private fun onLoginFormStateChanged(loginState: LoginFormState) {
    loginButton.isEnabled = loginState.isDataValid
    usernameInput.error = getErrorOrNull(this, loginState.usernameError)
    passwordInput.error = getErrorOrNull(this, loginState.passwordError)
  }

  private fun setupButtonsEvents() {
    settingsButton.setOnClickListener {
      startActivity(Intent(this, LoginSettingsActivity::class.java))
    }
    loginButton.setOnClickListener {
      loadingIndicator.visibility = View.VISIBLE
      loginViewModel.login(usernameInput.text.toString(), passwordInput.text.toString())
    }
  }

  private fun setupTextInputEvents() {
    usernameInput.afterTextChanged {
      loginViewModel.loginDataChanged(
        usernameInput.text.toString(),
        passwordInput.text.toString()
      )
    }

    passwordInput.afterTextChanged {
      loginViewModel.loginDataChanged(
        usernameInput.text.toString(),
        passwordInput.text.toString()
      )
    }
  }

  private fun startMainActivity() {
    startActivity(Intent(this, MainActivity::class.java))
  }

  override fun onResume() {
    super.onResume()
    loadApiSettingsFromPreferences(this)
  }

  private fun showLoginFailed(@StringRes errorString: Int) {
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
  }

  private fun showLoginSuccess(model: LoggedInUserView?) {
    model?.let {
      val welcome = getString(R.string.welcome)
      val displayName = model.displayName
      val message = "$welcome $displayName"
      Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }
  }
}
