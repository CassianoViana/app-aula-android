package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColor
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setProgressGradient
import kotlinx.android.synthetic.main.custom_progress_bar.view.*

class ProgressBar @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  fun setText(text: String) {
    label_below_progress_bar.text = text
  }

  fun setProgress(percentProgress: Int, progressColor: Int, color2: Int = R.color.progress_bar_incomplete_part) {
    setProgressGradient(
      progress_line,
      percentProgress,
      getColor(context, progressColor),
      getColor(context, color2)
    )
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_progress_bar, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.ProgressBar)
      label_below_progress_bar.text =
        styledAttributes.getString(R.styleable.ProgressBar_label_below_bar)
      styledAttributes.recycle()
    }

  }

}
