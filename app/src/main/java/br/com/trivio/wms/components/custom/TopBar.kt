package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_top_bar.view.*

class TopBar @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  fun onClickBack(onOptionClick: () -> Unit) {
    up_icon_button.setOnClickListener {
      onOptionClick()
    }
  }

  fun setText(text: String) {
    title_text_view.text = text
  }

  fun registerForOptionsMenu() {
    menu_top_bar_icon.setVisible(true)
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_top_bar, this, true)
    menu_top_bar_icon.setVisible(false)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopBar)
      val title = styledAttributes.getString(R.styleable.TopBar_title)
      title_text_view.text = title
      styledAttributes.recycle()
    }

  }

}
