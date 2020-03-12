package br.com.trivio.wms.ui.login

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import br.com.trivio.wms.MainActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.loadApiSettingsFromPreferences

class LoginActivity : AppCompatActivity() {

  private lateinit var loginViewModel: LoginViewModel

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_login)

    val username = findViewById<EditText>(R.id.username)
    val password = findViewById<EditText>(R.id.password)
    val login = findViewById<Button>(R.id.login)
    val loading = findViewById<ProgressBar>(R.id.loading)

    loginViewModel = ViewModelProviders.of(this, LoginViewModelFactory())
      .get(LoginViewModel::class.java)

    loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
      val loginState = it ?: return@Observer

      login.isEnabled = loginState.isDataValid

      fun getErrorOrNull(value: Int?): String? =
        if (value == null) null else this.getString(value)
      username.error = getErrorOrNull(loginState.usernameError)
      password.error = getErrorOrNull(loginState.passwordError)
    })

    loginViewModel.loginResult.observe(this@LoginActivity, Observer {
      val loginResult = it ?: return@Observer

      loading.visibility = View.GONE
      if (loginResult.error != null) {
        showLoginFailed(loginResult.error)
        return@Observer
      }
      if (loginResult.success != null) {
        updateUiWithUser(loginResult.success)
      }
      setResult(Activity.RESULT_OK)

      //Complete and destroy login activity once successful
      startMainActivity()
      finish()
    })

    username.afterTextChanged {
      loginViewModel.loginDataChanged(
        username.text.toString(),
        password.text.toString()
      )
    }

    password.apply {
      afterTextChanged {
        loginViewModel.loginDataChanged(
          username.text.toString(),
          password.text.toString()
        )
      }

      setOnEditorActionListener { _, actionId, _ ->
        when (actionId) {
          EditorInfo.IME_ACTION_DONE ->
            loginViewModel.login(
              username.text.toString(),
              password.text.toString()
            )
        }
        false
      }

      login.setOnClickListener {
        loading.visibility = View.VISIBLE
        loginViewModel.login(username.text.toString(), password.text.toString())
      }
    }
  }

  private fun startMainActivity() {
    startActivity(Intent(this, MainActivity::class.java))
  }

  override fun onResume() {
    super.onResume()
    loadApiSettingsFromPreferences(this)
  }

  private fun updateUiWithUser(model: LoggedInUserView) {
    val welcome = getString(R.string.welcome)
    val displayName = model.displayName
    // TODO : initiate successful logged in experience
    Toast.makeText(
      applicationContext,
      "$welcome $displayName",
      Toast.LENGTH_LONG
    ).show()
  }

  private fun showLoginFailed(@StringRes errorString: Int) {
    Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.login, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    when (item?.itemId) {
      R.id.action_settings_login -> startActivity(Intent(this, LoginSettingsActivity::class.java))
    }
    return super.onOptionsItemSelected(item)
  }

}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
  })
}
