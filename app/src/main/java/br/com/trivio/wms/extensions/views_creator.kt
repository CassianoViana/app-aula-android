package br.com.trivio.wms.extensions

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

fun ViewGroup.inflateToViewHolder(resourceId: Int): View {
  return LayoutInflater.from(this.context)
    .inflate(resourceId, this, false)
}
