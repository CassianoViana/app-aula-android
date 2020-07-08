package br.com.trivio.wms.extensions

import android.graphics.drawable.GradientDrawable
import android.view.View

fun setProgressGradient(v: View, percent: Int, colorStart: Int, colorEnd: Int) {
  val colors = mutableListOf<Int>()
  repeat((0..1000).count()) {
    colors.add(
      if (it < percent * 10) {
        colorStart
      } else {
        colorEnd
      }
    )
  }
  val linearGradient = GradientDrawable(
    GradientDrawable.Orientation.LEFT_RIGHT,
    colors.toIntArray()
  )
  v.background = linearGradient
}
