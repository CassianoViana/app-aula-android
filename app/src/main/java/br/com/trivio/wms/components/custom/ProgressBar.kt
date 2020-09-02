package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.getColor
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.Status
import br.com.trivio.wms.extensions.gradientDrawable
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_progress_bar.view.*

class ProgressBar @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {


  private var foregroundBarColor: Int = 0
  private var backgroundBarColor: Int = 0

  fun setText(text: String) {
    label_below_progress_bar.text = text
  }

  fun setOnClickListener(onClickListener: () -> Unit) {
    icon_finish_bar.setOnClickListener {
      onClickListener()
    }
  }

  fun setStatus(status: Status): Status {
    status.icon?.let { icon_finish_bar.setImageResource(it) }
    icon_finish_bar.background.setTint(getColor(context, status.color))
    icon_finish_bar.setVisible(status.icon != null)
    return status
  }

  fun setColors(colors: List<Int>) {
    gradientDrawable(colors)?.let {
      progress_line.background = it
    }
  }

  fun setProgress(percentProgress: Int) {
    val progressColors = mutableListOf<Int>()
    for (i in 1 until 100) {
      progressColors.add(
        if (i <= percentProgress) {
          this.foregroundBarColor
        } else {
          this.backgroundBarColor
        }
      )
    }
    setColors(progressColors)
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_progress_bar, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.ProgressBar)
      label_below_progress_bar.text =
        styledAttributes.getString(R.styleable.ProgressBar_label_below_bar)
      this.foregroundBarColor = styledAttributes.getColor(
        R.styleable.ProgressBar_foreground_color,
        getColor(context, R.color.colorLightBlueAccent)
      )
      this.backgroundBarColor = styledAttributes.getColor(
        R.styleable.ProgressBar_background_color,
        getColor(context, R.color.almost_transparent_2)
      )
      label_below_progress_bar.setVisible(
        styledAttributes.getBoolean(
          R.styleable.ProgressBar_show_label,
          true
        )
      )
      progress_line.layoutParams.height =
        styledAttributes.getDimensionPixelSize(R.styleable.ProgressBar_bar_height, 40)

      setProgress(
        styledAttributes.getInteger(R.styleable.ProgressBar_progress, 0)
      )
      styledAttributes.recycle()
    }
  }
}
