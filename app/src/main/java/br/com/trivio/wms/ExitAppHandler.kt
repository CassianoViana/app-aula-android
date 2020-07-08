package br.com.trivio.wms

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import br.com.trivio.wms.ui.login.LoginActivity

class ExitAppHandler(private val mainActivity: MainActivity) {
  fun logout() {
    globalData.token = null
    mainActivity.finish()
    mainActivity.startActivity(Intent(mainActivity, LoginActivity::class.java))
  }

  fun confirmExit(mainActivity: MainActivity) {
    AlertDialog.Builder(mainActivity)
      .setTitle(R.string.confirm_exit)
      .setPositiveButton(
        R.string.yes
      ) { _, _ -> mainActivity.finish() }
      .setNegativeButton(R.string.no, null)
      .show();
  }
}
