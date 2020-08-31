package br.com.trivio.wms.extensions

import android.view.Gravity.CENTER_VERTICAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.NumberInput
import java.math.BigDecimal

fun MyAppCompatActivity.createButton(name: String, onClick: (() -> Unit) = {}): Button {
  val button = inflate<Button>(R.layout.custom_button)
  button.text = name
  button.setOnClickListener {
    onClick()
  }
  return button
}

fun MyAppCompatActivity.createTextView(value: String): TextView {
  val textView = inflate<TextView>(R.layout.custom_text_view)
  textView.text = value
  return textView
}

fun MyAppCompatActivity.createInputNumber(
  value: BigDecimal? = null,
  label: String? = null
): NumberInput {
  val numberInput = NumberInput(this)
  value?.let {
    numberInput.setValue(it.toString())
  }
  label?.let {
    numberInput.setLabel(label)
  }
  return numberInput
}

fun MyAppCompatActivity.createLinearLayoutOf(vararg views: View): LinearLayout {
  val linearLayout = LinearLayout(this)
  linearLayout.orientation = LinearLayout.HORIZONTAL
  views.forEach {
    if (it is TextView) {
      it.gravity = CENTER_VERTICAL
      it.requestLayout()
    }
    linearLayout.addView(it)
  }
  return linearLayout
}

fun ViewGroup.inflateToViewHolder(resourceId: Int): View {
  return LayoutInflater.from(this.context)
    .inflate(resourceId, this, false)
}
