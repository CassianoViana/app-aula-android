package br.com.trivio.wms

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import br.com.trivio.wms.ui.login.LoginActivity

class LaunchActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_loading)
    login()
  }

  private fun login() {
    lifecycleScope.launchWhenCreated {
      val loadUserDetails = loadUserDetails()
      finish()
      if (loadUserDetails.isInvalid()) {
        startLoginActivity()
      } else {
        startMainActivity()
      }
    }
  }

  private fun startLoginActivity() {
    startActivity(Intent(this, LoginActivity::class.java))
  }

  private fun startMainActivity() {
    startActivity(Intent(this, MainActivity::class.java))
  }
}
