package br.com.trivio.wms

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.NavController
import androidx.navigation.NavDestination

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
