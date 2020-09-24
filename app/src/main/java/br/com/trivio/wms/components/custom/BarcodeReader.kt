package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.core.view.isVisible
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.extensions.toggleVisibility
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import kotlinx.android.synthetic.main.custom_barcode_reader.view.*

//https://github.com/yuriy-budiyev/code-scanner
class BarcodeReader @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var codeScanner: CodeScanner
  var codeScannerView: CodeScannerView
  var onRead: (read: String) -> Unit = { }
  var onError: (e: Exception) -> Unit = { }

  fun setOnReadListener(listener: (String) -> Unit) {
    this.onRead = listener
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_barcode_reader, this, true)
    codeScannerView = findViewById(R.id.scanner_view)
    codeScanner = CodeScanner(context, codeScannerView)

    codeScannerView.setOnClickListener {
      codeScanner.startPreview()
    }

    codeScanner.decodeCallback = DecodeCallback {
      if (isVisible)
        onRead(it.text)
    }
    codeScanner.errorCallback = ErrorCallback {
      onError(it)
    }

    setToggleable(false, hideScannerView = false)

    /*attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.Badge)
      val text = styledAttributes.getString(R.styleable.Badge_badge_text)
      val color =
        styledAttributes.getColor(
          R.styleable.Badge_badge_text_color,
          context.getColor(R.color.colorPrimaryDark)
        )
      styledAttributes.recycle()
    }*/
  }

  private fun updateToggleBtnText() {
    btn_toggle_barcode.text = if (scanner_view.isVisible) {
      context.getString(R.string.hide_barcode_reader)
    } else {
      context.getString(R.string.show_barcode_reader)
    }
  }

  fun setToggleable(toggleAble: Boolean = true, hideScannerView: Boolean = true) {
    btn_toggle_barcode.setVisible(toggleAble)
    scanner_view.setVisible(!hideScannerView)
    updateToggleBtnText()
    btn_toggle_barcode.setOnClickListener {
      scanner_view.toggleVisibility()
      updateToggleBtnText()
    }
  }

  fun setOnClickListener(onClickListener: () -> Any = {}) {
    codeScannerView.setOnClickListener {
      onClickListener()
    }
  }

  fun startRead() {
    codeScanner.startPreview()
  }

  fun pauseReading() {
    codeScanner.stopPreview()
  }

  fun stopReading() {
    pauseReading()
  }
}
