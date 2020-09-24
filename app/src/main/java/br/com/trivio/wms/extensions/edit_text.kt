package br.com.trivio.wms.extensions

import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
  this.addTextChangedListener(object : TextWatcher {
    override fun afterTextChanged(editable: Editable?) {
      afterTextChanged.invoke(editable.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
  })
}

fun EditText.moveCursorToEnd() {
  this.setSelection(this.text.length)
}

fun EditText.addOnDoneListener(listener: (String) -> Unit) {
  this.setOnEditorActionListener { textView, actionId, keyEvent ->
    if (actionId == EditorInfo.IME_ACTION_DONE) {
      listener(this.text.toString())
    }
    true
  }
}
