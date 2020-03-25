package br.com.trivio.wms.ui.conference.cargo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import br.com.trivio.wms.R
import br.com.trivio.wms.handleHomeClickFinish
import br.com.trivio.wms.setupToolbar

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
