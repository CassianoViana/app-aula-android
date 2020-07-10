package br.com.trivio.wms.extensions

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R

@SuppressLint("WrongViewCast")
fun MyAppCompatActivity.getProgressBarLayout(): FrameLayout? {
  return findViewById(R.id.layout_progress_bar)
}

fun MyAppCompatActivity.startLoading(loadingStringId: Int? = null) {
  getProgressBarLayout()?.let { layout ->
    layout.visibility = View.VISIBLE
    loadingStringId?.let {
      layout.findViewById<TextView>(R.id.loading_text).text = getString(it)
    }
  }
}

fun MyAppCompatActivity.endLoading() {
  getProgressBarLayout()?.visibility = View.GONE
}

fun TextView.setLoading(value: Boolean) {
  background = if (value) {
    text = ""
    resources.getDrawable(R.drawable.background_loading_grey_input)
  } else {
    resources.getDrawable(R.drawable.transparent)
  }
}
