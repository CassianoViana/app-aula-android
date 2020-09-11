package br.com.trivio.wms

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.trivio.wms.components.custom.TopBar
import br.com.trivio.wms.extensions.handleHomeClickFinish


abstract class MyAppCompatActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  override fun onResume() {
    super.onResume()
    this.setupToolbar()
  }

  fun setupToolbar() {
    val topBar = findViewById<TopBar?>(R.id.top_bar)
    if (topBar != null) {
      topBar.onClickBack {
        onFinish()
      }
    }
  }

  open fun onFinish() {
    finish()
  }

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

  open fun startLoading(loadingStringId: Int? = null) {
    getProgressBarLayout()?.let { layout ->
      layout.visibility = View.VISIBLE
      loadingStringId?.let {
        layout.findViewById<TextView>(R.id.loading_text).text = getString(it)
      }
    }
  }

  open fun endLoading() {
    getProgressBarLayout()?.visibility = View.GONE
  }

  @SuppressLint("WrongViewCast")
  fun getProgressBarLayout(): View? {
    return findViewById(R.id.layout_progress_bar)
  }
}


