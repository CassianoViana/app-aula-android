package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setLoading

class LabelledInfo @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var text: String? = ""
    set(value) {
      valueTextView.text = value
      loading = false
      field = value
    }
  private var titleTextView: TextView
  private var valueTextView: TextView

  var loading: Boolean = false
    set(value) {
      valueTextView.setLoading(value)
      field = value
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_labelled_info, this, true)
    titleTextView = findViewById(R.id.title)
    valueTextView = findViewById(R.id.value)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.LabelledInfo)
      val title = styledAttributes.getString(R.styleable.LabelledInfo_label_title)
      val value = styledAttributes.getString(R.styleable.LabelledInfo_label_value)

      titleTextView.text = title
      valueTextView.text = value

      styledAttributes.recycle()
    }
  }
}