package br.com.trivio.wms

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import br.com.trivio.wms.data.model.UserDetails
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.delay


class MainActivity : AppCompatActivity() {

  companion object {
    const val LOGGING = "LOGGING"
  }

  private lateinit var exitAppHandler: ExitAppHandler
  private lateinit var appBarConfiguration: AppBarConfiguration

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    val toolbar: Toolbar = findViewById(R.id.toolbar_main)
    setSupportActionBar(toolbar)

    val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
    val navView: NavigationView = findViewById(R.id.nav_view)

    val navController = findNavController(R.id.nav_host_fragment)
    // Passing each menu ID as a set of Ids because each
    // menu should be considered as top level destinations.
    appBarConfiguration = AppBarConfiguration(
      setOf(
        R.id.nav_tasks, R.id.nav_gallery, R.id.nav_slideshow,
        R.id.nav_tools, R.id.nav_share, R.id.nav_exit
      ), drawerLayout
    )
    exitAppHandler = ExitAppHandler(this)
    navController.addOnDestinationChangedListener(exitAppHandler)
    setupActionBarWithNavController(navController, appBarConfiguration)
    navView.setupWithNavController(navController)

    lifecycleScope.launchWhenCreated {
      delay(500L)
      globalData.userDetails?.let {
        updateHeaderUserDetailsUI(it)
      }
    }
  }

  override fun onBackPressed() {
    exitAppHandler.confirmExit(this)
  }

  private fun updateHeaderUserDetailsUI(userDetails: UserDetails) {
    findViewById<TextView>(R.id.nav_header_title)?.apply {
      text = userDetails.name
    }
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.main, menu)
    return true
  }

  override fun onSupportNavigateUp(): Boolean {
    val navController = findNavController(R.id.nav_host_fragment)
    return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
  }
}
