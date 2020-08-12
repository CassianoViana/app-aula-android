package br.com.trivio.wms.extensions

import android.os.Handler

fun delay(millis: Long = 500, fn: () -> Unit) {
  Handler().postDelayed({ fn() }, millis)
}
