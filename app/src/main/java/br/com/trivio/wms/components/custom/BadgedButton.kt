package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.setPadding
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_badged_button.view.*

class BadgedButton @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  /*var backgroundColor: String? = "#FFFFFF"
    set(value) {
      valueTextView.setTagBackground(value)
      field = value
    }*/

  var badgeValue: String = ""
    set(value) {
      badge_button_count.text = value
      field = value
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_badged_button, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.BadgedButton)
      val badgeValue = styledAttributes.getString(R.styleable.BadgedButton_value)
      badgeValue?.let {
        this.badgeValue = it
      }
      badge_button_count.setVisible(badgeValue != null && badgeValue.isNotEmpty())

      badge_text.setPadding(
        styledAttributes.getDimensionPixelSize(R.styleable.BadgedButton_btn_padding, 10)
      )
      badge_text.setImageDrawable(styledAttributes.getDrawable(R.styleable.BadgedButton_btn_drawable))
      styledAttributes.recycle()
    }
  }
}
