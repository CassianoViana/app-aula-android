package br.com.trivio.wms.extensions

import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import br.com.trivio.wms.R

fun AppCompatActivity.createButton(name: String, onClick: (() -> Unit) = {}): Button {
  val button = inflate<Button>(R.layout.button)
  button.text = name
  button.setOnClickListener {
    onClick()
  }
  return button
}
