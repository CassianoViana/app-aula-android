package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
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

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_barcode_reader, this, true)
    codeScannerView = findViewById(R.id.scanner_view)
    codeScanner = CodeScanner(context, codeScannerView)

    codeScannerView.setOnClickListener {
      codeScanner.startPreview()
    }

    codeScanner.decodeCallback = DecodeCallback {
      setInputResultValue(it.text)
      if (isVisible)
        onRead(it.text)
    }
    codeScanner.errorCallback = ErrorCallback {
      onError(it)
    }

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.BarcodeReader)

      val toggleAble = styledAttributes.getBoolean(
        R.styleable.BarcodeReader_barcode_reader_show_btn_toggle_visibility,
        false
      )

      val showInput = styledAttributes.getBoolean(
        R.styleable.BarcodeReader_barcode_reader_show_input,
        false
      )

      val inputHint = styledAttributes.getString(
        R.styleable.BarcodeReader_barcode_reader_input_hint
      )

      setHint(inputHint)
      setInputVisible(showInput)
      setToggleable(toggleAble, hideScannerView = false)

      styledAttributes.recycle()
    }
  }

  /*private fun updateToggleBtnText() {
    btn_toggle_barcode.text = if (scanner_view.isVisible) {
      context.getString(R.string.hide_barcode_reader)
    } else {
      context.getString(R.string.show_barcode_reader)
    }
  }*/

  private fun setInputResultValue(text: String?) {
    input_barcode_result.setText(text)
  }

  fun setInputVisible(visible: Boolean = true) {
    input_barcode_result.setVisible(visible)
  }

  fun setHint(hint: String?) {
    hint?.let {
      input_barcode_result.hint = it
    }
  }

  fun setToggleable(toggleAble: Boolean = true, hideScannerView: Boolean = true) {
    btn_toggle_barcode.setVisible(toggleAble)
    scanner_view.setVisible(!hideScannerView)
    btn_toggle_barcode.setOnClickListener {
      scanner_view.toggleVisibility()
    }
  }

  fun setOnReadListener(listener: (String) -> Unit) {
    this.onRead = listener
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

  fun getInput(): EditText {
    return input_barcode_result
  }
}
