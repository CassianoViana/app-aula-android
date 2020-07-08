package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import br.com.trivio.wms.R

class TopBar @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  fun onClickBack(onOptionClick: () -> Unit) {
    upIconButton.setOnClickListener {
      onOptionClick()
    }
  }

  fun setTitle(title: String) {
    titleTextView.text = title
  }

  private var titleTextView: TextView
  private var upIconButton: ImageButton

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_top_bar, this, true)
    titleTextView = findViewById<EditText>(R.id.title_text_view)
    upIconButton = findViewById<ImageButton>(R.id.up_icon_button)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.TopBar)
      val title = styledAttributes.getString(R.styleable.TopBar_title)
      titleTextView.text = title
      styledAttributes.recycle()
    }

  }

}
