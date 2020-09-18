package br.com.trivio.wms.extensions

import android.view.View
import android.widget.LinearLayout

fun View.setWeight(weight: Int = 1) {
  layoutParams = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT,
    weight.toFloat()
  )
}
