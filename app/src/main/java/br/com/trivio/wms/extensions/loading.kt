package br.com.trivio.wms.extensions

import android.annotation.SuppressLint
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R


fun View.setLoading(value: Boolean) {
  background = if (value) {
    if (this is ImageView) {
      resources.getDrawable(R.drawable.background_loading_grey_input_rounded)
    } else {
      resources.getDrawable(R.drawable.background_loading_grey_input)
    }
  } else {
    resources.getDrawable(R.drawable.transparent)
  }
  foreground = background
}
