package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setLoading
import kotlinx.android.synthetic.main.custom_labelled_icon.view.*
import kotlinx.android.synthetic.main.custom_labelled_number.view.*

class LabelledIcon @JvmOverloads constructor(
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
    LayoutInflater.from(context).inflate(R.layout.custom_labelled_icon, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.LabelledIcon)
      val label = styledAttributes.getString(R.styleable.LabelledIcon_labelled_icon_label)
      val src = styledAttributes.getResourceId(
        R.styleable.LabelledIcon_labelled_icon_src,
        R.drawable.ic_baseline_check_circle_30
      )

      val color = styledAttributes.getColor(
        R.styleable.LabelledIcon_labelled_icon_color,
        context.getColor(R.color.colorDark)
      )

      labelled_icon_label.text = label
      labelled_icon_img.setImageResource(src)
      labelled_icon_label.setTextColor(color)
      labelled_icon_img.drawable.setTint(color)

      styledAttributes.recycle()
    }
  }
}
