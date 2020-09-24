package br.com.trivio.wms.extensions

import android.view.Gravity.CENTER_VERTICAL
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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

fun MyAppCompatActivity.createTextView(
  value: String,
  small: Boolean = false,
  colorResourceId: Int? = null
): TextView {
  val textView = if (!small) {
    inflate<TextView>(R.layout.custom_text_view)
  } else {
    inflate<TextView>(R.layout.custom_small_text_view)
  }
  colorResourceId?.let {
    textView.setTextColor(getColor(it))
  }
  textView.text = value
  return textView
}

fun MyAppCompatActivity.createEditText(
  value: String? = null,
  hint: String? = null
): EditText {
  val editText = inflate<EditText>(R.layout.custom_edit_text)
  value?.let {
    editText.setText(it)
  }
  hint?.let {
    editText.hint = it
  }
  return editText
}

fun MyAppCompatActivity.createInputNumber(
  value: BigDecimal? = BigDecimal.ZERO,
  labelBeforeInput: String? = null,
  labelAfterInput: String? = null,
  allowNegative: Boolean = true,
  backgroundDrawableResourceId: Int? = null
): NumberInput {
  val numberInput = NumberInput(this)
  backgroundDrawableResourceId?.let {
    numberInput.setButtonsDrawable(it)
  }
  numberInput.allowNegative = allowNegative

  value?.let {
    numberInput.setValue(it.toString())
  }
  labelBeforeInput?.let {
    numberInput.setLabel(labelBeforeInput)
  }
  labelAfterInput?.let {
    numberInput.setLabelAfterInput(labelAfterInput)
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
