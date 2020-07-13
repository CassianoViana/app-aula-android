package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColor
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.Status
import br.com.trivio.wms.extensions.Status.Companion.NOT_COMPLETED
import br.com.trivio.wms.extensions.getProgressGradient
import br.com.trivio.wms.extensions.gradientDrawable
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_progress_bar.view.*

class ProgressBar @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


  fun setText(text: String) {
    label_below_progress_bar.text = text
  }

  fun setOnClickListener(onClickListener: () -> Unit) {
    icon_finish_bar.setOnClickListener {
      onClickListener()
    }
  }

  fun setProgress(
    percentProgress: Int,
    status: Status = NOT_COMPLETED,
    backgroundBarColor: Int = R.color.progress_bar_incomplete_part
  ) {
    val progressStatus = setStatus(status)
    progress_line.background = getProgressGradient(
      percentProgress,
      getColor(context, progressStatus.color),
      getColor(context, backgroundBarColor)
    )
  }

  fun setStatus(status: Status): Status {
    status.icon?.let { icon_finish_bar.setImageResource(it) }
    icon_finish_bar.background.setTint(getColor(context, status.color))
    icon_finish_bar.setVisible(status.icon != null)
    return status
  }

  fun setColors(colors: List<Int>) {
    progress_line.background = gradientDrawable(colors)
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
