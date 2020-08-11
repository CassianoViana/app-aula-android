package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import br.com.trivio.wms.R
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback

class BarcodeReader @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var codeScanner: CodeScanner
  private var codeScannerView: CodeScannerView
  var onRead: (read: String) -> Unit = { }
  var onError: (e: Exception) -> Unit = { }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_barcode_reader, this, true)
    codeScannerView = findViewById(R.id.scanner_view)
    codeScanner = CodeScanner(context, codeScannerView)

    codeScanner.decodeCallback = DecodeCallback {
      onRead(it.text)
    }
    codeScanner.errorCallback = ErrorCallback {
      onError(it)
    }

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

  fun start() {
    codeScanner.startPreview()
  }

  fun pause() {
    codeScanner.stopPreview()
  }
}
