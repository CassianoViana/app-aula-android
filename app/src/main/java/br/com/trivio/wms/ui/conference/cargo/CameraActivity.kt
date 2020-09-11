package br.com.trivio.wms.ui.conference.cargo

import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.ui.tools.ImagePicker
import kotlinx.android.synthetic.main.activity_camera.*


class CameraActivity : MyAppCompatActivity() {

  companion object {
    const val REQUEST_CAPTURE_IMAGE = 1
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_camera)
    camera_image_view.setOnClickListener {
      requestOpenCamera()
    }
    requestOpenCamera()
  }

  private fun requestOpenCamera() {
    if (requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
      if (requestPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
        openCamera()
      }
    }
  }

  private fun openCamera() {
    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
    if (cameraIntent.resolveActivity(packageManager) != null) {
      startActivityForResult(cameraIntent, REQUEST_CAPTURE_IMAGE)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    data?.let {
      val bitmap = data.extras.get("data") as Bitmap
      camera_image_view.setImageBitmap(ImagePicker.rotate(bitmap, 90))
    }
  }

}
