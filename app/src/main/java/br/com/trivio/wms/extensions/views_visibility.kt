package br.com.trivio.wms.extensions

import android.view.View

fun View.setVisible(visible: Boolean = true) {
  visibility = if (visible) {
    View.VISIBLE
  } else {
    View.GONE
  }
}
