package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import br.com.trivio.wms.R
import setTagBackground

class Badge @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var backgroundColor: String? = "#FFFFFF"
    set(value) {
      valueTextView.setTagBackground(value)
      field = value
    }

  var text: String? = ""
    set(value) {
      valueTextView.text = value
      field = value
    }
  private var valueTextView: TextView

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_badge, this, true)
    valueTextView = findViewById(R.id.badge_text)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.Badge)
      val text = styledAttributes.getString(R.styleable.Badge_badge_text)
      val color =
        styledAttributes.getColor(
          R.styleable.Badge_badge_text_color,
          context.getColor(R.color.colorPrimaryDark)
        )
      valueTextView.text = text
      valueTextView.setTextColor(color)
      styledAttributes.recycle()
    }
  }
}
