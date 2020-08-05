package br.com.trivio.wms.extensions

import br.com.trivio.wms.R

class Status(
  val icon: Int? = null,
  val bigIcon: Int = 0,
  val color: Int = R.color.almost_transparent_2
) {
  companion object {
    val SUCCESS = Status(
      icon = R.drawable.ic_check_black_24dp,
      bigIcon = R.drawable.ic_check_big_green,
      color = R.color.success
    )
    val ERROR = Status(
      icon = R.drawable.ic_close_black_24dp,
      bigIcon = R.drawable.ic_close_red_big,
      color = R.color.error
    )
    val NOT_COMPLETED = Status(
      icon = null,
      bigIcon = R.drawable.ic_close_red_big
    )
  }
}
