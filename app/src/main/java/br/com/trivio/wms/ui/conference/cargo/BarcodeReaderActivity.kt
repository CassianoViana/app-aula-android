package br.com.trivio.wms.ui.conference.cargo

import android.Manifest
import android.content.Intent
import android.os.Bundle
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.showMessageError
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import kotlinx.android.synthetic.main.activity_barcode_reader.*

class BarcodeReaderActivity : MyAppCompatActivity() {

  companion object {
    const val RESULT_BARCODE = "RESULT_BARCODE"
    const val BARCODE_ACTIVITY = 300
  }

  //https://github.com/yuriy-budiyev/code-scanner
  private lateinit var codeScanner: CodeScanner

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_barcode_reader)

    codeScanner = CodeScanner(this, scanner_view)

    codeScanner.decodeCallback = DecodeCallback {
      val result = Intent()
      result.putExtra(RESULT_BARCODE, it.text)
      setResult(BARCODE_ACTIVITY, result)
      finish()
    }
    codeScanner.errorCallback = ErrorCallback {
      runOnUiThread {
        showMessageError(it.toString())
      }
    }

    scanner_view.setOnClickListener {
      codeScanner.startPreview()
    }
  }

  override fun onResume() {
    super.onResume()
    if (checkCameraPermissions(Manifest.permission.CAMERA)) {
      codeScanner.startPreview()
    }
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    codeScanner.startPreview()
  }

  override fun onPause() {
    codeScanner.releaseResources()
    super.onPause()
  }
}
