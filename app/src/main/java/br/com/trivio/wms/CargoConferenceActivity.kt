package br.com.trivio.wms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem

class CargoConferenceActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargo_conference)
    this.setupToolbar(R.string.cargo_conference)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }
}
