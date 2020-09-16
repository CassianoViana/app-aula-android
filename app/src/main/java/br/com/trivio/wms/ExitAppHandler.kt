package br.com.trivio.wms

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import br.com.trivio.wms.ui.login.LoginActivity

class ExitAppHandler(private val menuActivity: MenuActivity) {
  fun logout() {
    globalData.token = null
    menuActivity.finish()
    menuActivity.startActivity(Intent(menuActivity, LoginActivity::class.java))
  }

  fun confirmExit(menuActivity: MenuActivity) {
    AlertDialog.Builder(menuActivity)
      .setTitle(R.string.confirm_exit)
      .setPositiveButton(
        R.string.yes
      ) { _, _ -> menuActivity.finish() }
      .setNegativeButton(R.string.no, null)
      .show();
  }
}
