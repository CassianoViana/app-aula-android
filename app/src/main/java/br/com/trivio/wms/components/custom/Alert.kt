package br.com.trivio.wms.components.custom

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.com.trivio.wms.R
import kotlinx.android.synthetic.main.custom_alert.view.*

class Alert @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  var message: String = ""
    set(value) {
      alert_message.text = value
      field = value
    }

  var icon: Drawable? = null
    set(value) {
      alert_icon.setImageDrawable(value)
    }

  companion object {
    const val TYPE_WARNING = "warning"
    const val TYPE_INFO = "info"
    /*const val TYPE_WARNING = "warning"
    const val TYPE_WARNING = "warning"*/
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_alert, this, true)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.Alert)
      styledAttributes.getString(R.styleable.Alert_alert_text)?.let {
        this.message = it
      }
      styledAttributes.getString(R.styleable.Alert_alert_type).let {
        setType(it)
      }
      styledAttributes.recycle()
    }
  }

  fun setType(it: String?) {
    when (it) {
      TYPE_WARNING -> {
        icon = context.getDrawable(R.drawable.ic_baseline_warning_30)
        background = context.getDrawable(R.drawable.light_yellow_rounded)
      }
      TYPE_INFO -> {
        icon = context.getDrawable(R.drawable.ic_baseline_info_30)
        background = context.getDrawable(R.drawable.light_blue_rounded)
      }
    }
  }
}
