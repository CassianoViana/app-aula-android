package br.com.trivio.wms

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Handler
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.trivio.wms.extensions.handleHomeClickFinish


abstract class MyAppCompatActivity : AppCompatActivity() {

  fun startTask() {
    getSharedPreferences().edit()
      .putString("TASK_ACTIVITY", this.javaClass.canonicalName)
      .apply()
  }

  fun clearTop() {
    getSharedPreferences().getString("TASK_ACTIVITY", null)?.let {
      val activityClass = Class.forName(it).kotlin
      val intent = Intent(applicationContext, activityClass.java)
      intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
      startActivity(intent)
    }
  }

  private fun getSharedPreferences() =
    applicationContext.getSharedPreferences("TASK", Context.MODE_PRIVATE)

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }

  fun checkCameraPermissions(camera: String): Boolean {
    var permitted = true
    if (ContextCompat.checkSelfPermission(this, camera)
      == PackageManager.PERMISSION_DENIED
    ) {
      permitted = false
      ActivityCompat.requestPermissions(this, arrayOf(camera), 100)
    }
    return permitted
  }
}


