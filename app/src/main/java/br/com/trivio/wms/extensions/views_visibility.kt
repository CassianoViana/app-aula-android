package br.com.trivio.wms.extensions

import android.view.View

fun View.toggleVisibility(): Int {
  if (this.visibility == View.VISIBLE) {
    this.visibility = View.GONE
  } else {
    this.visibility = View.VISIBLE
  }
  return this.visibility
}

fun View.setVisible(visible: Boolean = true) {
  visibility = if (visible) {
    View.VISIBLE
  } else {
    View.GONE
  }
}
