package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setLoading
import kotlinx.android.synthetic.main.custom_labelled_number.view.*

class LabelledNumber @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var value: String? = ""
    set(value) {
      labelled_number_number.text = value
      loading = false
      field = value
    }

  var loading: Boolean = false
    set(value) {
      labelled_number_label.setLoading(value)
      labelled_number_number.setLoading(value)
      field = value
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_labelled_number, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.LabelledNumber)
      val title = styledAttributes.getString(R.styleable.LabelledNumber_labelled_number_label)
      val value = styledAttributes.getString(R.styleable.LabelledNumber_labelled_number_value)

      val color = styledAttributes.getColor(
        R.styleable.LabelledNumber_labelled_number_color,
        context.getColor(R.color.colorDark)
      )

      labelled_number_label.text = title
      labelled_number_number.text = value
      labelled_number_label.setTextColor(color)
      labelled_number_number.setTextColor(color)

      styledAttributes.recycle()
    }
  }
}
