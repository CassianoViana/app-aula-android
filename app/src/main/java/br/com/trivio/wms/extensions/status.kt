package br.com.trivio.wms.extensions

import br.com.trivio.wms.R

data class Status(
  val name: String = "",
  val icon: Int? = null,
  val bigIcon: Int = 0,
  val color: Int = R.color.almost_transparent_2
) {
  companion object {
    val SUCCESS = Status(
      name = "Success",
      icon = R.drawable.ic_check_black_24dp,
      bigIcon = R.drawable.ic_check_big_green,
      color = R.color.success
    )
    val ERROR = Status(
      name = "Error",
      icon = R.drawable.ic_close_black_24dp,
      bigIcon = R.drawable.ic_close_red_big,
      color = R.color.error
    )
    val NOT_COMPLETED = Status(
      name = "Undefined",
      icon = null,
      bigIcon = android.R.color.transparent
    )
  }
}
