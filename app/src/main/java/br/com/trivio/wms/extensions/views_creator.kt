package br.com.trivio.wms.extensions

import android.widget.Button
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R

fun MyAppCompatActivity.createButton(name: String, onClick: (() -> Unit) = {}): Button {
  val button = inflate<Button>(R.layout.custom_button)
  button.text = name
  button.setOnClickListener {
    onClick()
  }
  return button
}
