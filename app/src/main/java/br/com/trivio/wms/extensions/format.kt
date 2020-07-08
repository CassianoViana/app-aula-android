package br.com.trivio.wms.extensions

import android.annotation.SuppressLint
import br.com.trivio.wms.globalData
import org.joda.time.LocalDateTime
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

fun formatNumber(number: BigDecimal?): String {
  if (number == null) return "0"
  return NumberFormat.getInstance().format(number)
}

fun coalesce(value1: String?, alternative: Int): String {
  if (!value1.isNullOrBlank()) {
    return value1
  }
  return globalData.appContext.getString(alternative)
}

@SuppressLint("SimpleDateFormat")
fun LocalDateTime.formatTo(s: String): CharSequence? {
  val c = Calendar.getInstance();
  c[Calendar.YEAR] = this.year
  c[Calendar.DAY_OF_MONTH] = this.dayOfMonth
  c[Calendar.HOUR_OF_DAY] = this.hourOfDay
  c[Calendar.MINUTE] = this.minuteOfHour
  c[Calendar.SECOND] = this.secondOfMinute
  c[Calendar.MILLISECOND] = this.millisOfSecond
  return SimpleDateFormat(s).format(c.time)
}

fun getPercent(a: Int, b: Int): Int {
  return when (b) {
      0 -> 0
      else -> ((a.toFloat() / b) * 100).roundToInt()
  }
}
