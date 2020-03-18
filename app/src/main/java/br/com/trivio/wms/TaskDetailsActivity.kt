package br.com.trivio.wms

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.lifecycleScope

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    val id = intent.getLongExtra(TASK_ID, 0)

    lifecycleScope.launchWhenCreated {
      loadTask(id)
    }
  }
}
