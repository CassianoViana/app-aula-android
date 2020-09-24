package br.com.trivio.wms.extensions

import android.view.View
import android.widget.LinearLayout

fun View.setMarginVertical(margin: Int = 20, height: Int? = null) {
  layoutParams = LinearLayout.LayoutParams(
    LinearLayout.LayoutParams.MATCH_PARENT,
    LinearLayout.LayoutParams.WRAP_CONTENT,
  ).apply {
    topMargin = margin
    bottomMargin = margin
    height?.let{
      this.height = height
    }
  }
}
