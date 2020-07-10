package br.com.trivio.wms.extensions

import androidx.fragment.app.Fragment
import br.com.trivio.wms.MyAppCompatActivity

fun Fragment.startLoading() {
  (activity as MyAppCompatActivity).startLoading()
}

fun Fragment.endLoading() {
  (activity as MyAppCompatActivity).endLoading()
}
