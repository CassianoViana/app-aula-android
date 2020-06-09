package br.com.trivio.wms.extensions

import android.annotation.SuppressLint
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.trivio.wms.R

@SuppressLint("WrongViewCast")
fun AppCompatActivity.getProgressBarLayout(): FrameLayout? {
  return findViewById(R.id.layout_progress_bar)
}

fun AppCompatActivity.startLoading(loadingStringId: Int? = null) {
  getProgressBarLayout()?.let { layout ->
    layout.visibility = View.VISIBLE
    loadingStringId?.let {
      layout.findViewById<TextView>(R.id.loading_text).text = getString(it)
    }
  }
}

fun AppCompatActivity.endLoading() {
  getProgressBarLayout()?.visibility = View.GONE
}
