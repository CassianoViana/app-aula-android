package br.com.trivio.wms.extensions

import android.graphics.drawable.GradientDrawable

fun getProgressGradient(percent: Int, colorStart: Int, colorEnd: Int): GradientDrawable {
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
  return gradientDrawable(colors)!!
}

fun gradientDrawable(colors: List<Int>): GradientDrawable? {
  if(colors.size <=1) return null
  val c = mutableListOf<Int>()
  repeat((0..1000).count()) {
    val index = it * colors.size / 1000
    if (index < colors.size)
      c.add(colors[index])
  }
  return GradientDrawable(
    GradientDrawable.Orientation.LEFT_RIGHT,
    c.toIntArray()
  )

  /*
  indice
  * */
}
