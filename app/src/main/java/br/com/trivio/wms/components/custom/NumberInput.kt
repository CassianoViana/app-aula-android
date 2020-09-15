package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.coalesce
import br.com.trivio.wms.extensions.moveCursorToEnd
import br.com.trivio.wms.extensions.setKeyboardVisible
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_number_input.view.*
import java.math.BigDecimal

class NumberInput @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var allowNegative: Boolean = true

  var textSize: Float
    get() {
      return custom_number_input.textSize
    }
    set(value) {
      custom_number_input.textSize = value
    }

  val value: BigDecimal?
    get() {
      var stringValue = custom_number_input.text.toString()
      if (stringValue.isEmpty()) {
        stringValue = "0"
      }
      return BigDecimal(stringValue)
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_number_input, this, true)

    custom_number_input_label.setVisible(false)
    custom_number_input_label_after_input.setVisible(false)
    icon_add.setOnClickListener {
      this.add(1)
    }
    icon_remove.setOnClickListener {
      this.add(-1)
    }

    attrs?.let {
      /*val styledAttributes = context.obtainStyledAttributes(it, R.styleable.SearchInput)
      val hint = styledAttributes.getString(R.styleable.SearchInput_hint)
      custom_number_input.hint = hint
      styledAttributes.recycle()*/
    }

  }

  private fun add(quantitySum: Int) {
    val value = custom_number_input.text.toString().coalesce("0")
    val sum = value.toInt() + quantitySum
    if (sum < 0 && !this.allowNegative) {
      return
    }
    setValue(sum.toString())
  }

  fun addTextChangedListener(listener: (String) -> Unit) {
    custom_number_input.addTextChangedListener {
      listener(custom_number_input.text.toString())
    }
  }

  fun addOnDoneListener(listener: (String) -> Unit) {
    custom_number_input.setOnEditorActionListener { textView, actionId, keyEvent ->
      if (actionId == EditorInfo.IME_ACTION_DONE) {
        listener(custom_number_input.text.toString())
      }
      true
    }
  }

  fun setLabel(label: String) {
    custom_number_input_label.setVisible(true)
    custom_number_input_label.text = label
  }

  fun setButtonsDrawable(drawableId: Int) {
    icon_add.background = context.getDrawable(drawableId)
    icon_remove.background = context.getDrawable(drawableId)
  }

  fun setLabelAfterInput(label: String) {
    custom_number_input_label_after_input.setVisible(true)
    custom_number_input_label_after_input.text = label
  }

  fun setValue(value: BigDecimal?) {
    value?.let {
      this.setValue(it.toInt().toString())
    }
  }

  fun setValue(value: String) {
    custom_number_input.setText(value)
    custom_number_input.moveCursorToEnd()
  }

  fun showKeyboard() {
    custom_number_input.setKeyboardVisible(context)
  }
}
