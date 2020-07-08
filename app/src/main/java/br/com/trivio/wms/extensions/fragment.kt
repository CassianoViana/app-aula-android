package br.com.trivio.wms.extensions

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.startLoading() {
  (activity as AppCompatActivity).startLoading()
}

fun Fragment.endLoading() {
  (activity as AppCompatActivity).endLoading()
}
