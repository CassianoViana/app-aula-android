package br.com.trivio.wms.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import br.com.trivio.wms.R

fun AppCompatActivity.setupToolbar(resourceStringTitle: Int = 0, showHomeButton: Boolean = true) {
  val toolbar = this.findViewById<Toolbar>(R.id.toolbar)
  if (resourceStringTitle != 0)
    toolbar.title = this.getString(resourceStringTitle)
  this.setSupportActionBar(toolbar)
  this.supportActionBar?.setDisplayHomeAsUpEnabled(showHomeButton)
}
