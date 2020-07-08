package br.com.trivio.wms.extensions

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import br.com.trivio.wms.MyAppCompatActivity

fun MyAppCompatActivity.showKeyboard(editText: EditText? = null) {
  val imm: InputMethodManager =
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
  editText?.requestFocus()
  editText?.selectAll()
}

fun MyAppCompatActivity.hideKeyboard(editText: EditText) {
  val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(editText.windowToken, 0)
}
