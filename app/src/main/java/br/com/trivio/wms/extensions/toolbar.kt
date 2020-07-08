package br.com.trivio.wms.extensions

import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.TopBar

fun MyAppCompatActivity.setupToolbar() {
  val topBar = findViewById<TopBar>(R.id.top_bar)
  topBar.let {
    topBar.onClickBack {
      finish()
    }
  }
}
