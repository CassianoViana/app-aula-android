package br.com.trivio.wms

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import br.com.trivio.wms.extensions.handleHomeClickFinish
import kotlin.reflect.KClass


abstract class MyAppCompatActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
  }

  fun <T : MyAppCompatActivity> clearTop(activityClass: KClass<T>) {
    val intent = Intent(applicationContext, activityClass.java)
    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
    startActivity(intent)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }
}


