package br.com.trivio.wms.extensions

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText

fun EditText.setKeyboardVisible(context: Context, visible: Boolean = true) {
  val imm: InputMethodManager =
    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  if (visible) {
    imm.showSoftInput(this, 0)
    requestFocus()
    selectAll()
  } else {
    imm.hideSoftInputFromWindow(windowToken, 0)
  }
}
