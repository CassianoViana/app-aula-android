package br.com.trivio.wms.components.custom;

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import br.com.trivio.wms.R

class BarcodeReaderConnectionIndicator @JvmOverloads
constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var label: TextView
  private var icon: ImageView

  var connected: Boolean = false
    set(value) {
      updateIcon(value)
      updateLabel(value)
      field = value
    }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_coletor_indicator, this, true)
    label = findViewById(R.id.label)
    icon = findViewById(R.id.icon)
    connected = false
  }

  private fun updateLabel(connected: Boolean) {
    icon.setImageResource(
      when {
        connected -> R.drawable.barcode_on
        else -> R.drawable.barcode_off
      }
    )
  }

  private fun updateIcon(connected: Boolean) {
    label.text = context.getString(
      when {
        connected -> R.string.barcode_reader_connection_established
        else -> R.string.barcode_reader_connection_failed
      }
    )
  }

}
