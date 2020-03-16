package br.com.trivio.wms

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import br.com.trivio.wms.ui.login.LoginActivity

class ExitAppHandler(private val mainActivity: MainActivity) :
  NavController.OnDestinationChangedListener {

  override fun onDestinationChanged(
    controller: NavController,
    destination: NavDestination,
    arguments: Bundle?
  ) {
    if (destination.label == mainActivity.getString(R.string.menu_exit)) {
      globalData.token = null
      mainActivity.finish()
      mainActivity.startActivity(Intent(mainActivity, LoginActivity::class.java))
    }
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
