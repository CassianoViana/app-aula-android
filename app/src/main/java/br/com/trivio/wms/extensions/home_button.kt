package br.com.trivio.wms.extensions

import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity

fun AppCompatActivity.handleHomeClickFinish(
  item: MenuItem?,
  function: (() -> Unit)? = null
) {
  when (item?.itemId) {
    android.R.id.home -> {
      function?.let {
        it()
      }
      finish()
    }
  }
}
