package br.com.trivio.wms

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import br.com.trivio.wms.ui.tasks.TaskDetailsViewModel

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    val viewModel: TaskDetailsViewModel by viewModels()

    val taskName = findViewById<TextView>(R.id.task_name)
    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    setSupportActionBar(toolbar)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)

    viewModel.task.observe(this, Observer {
      threatResult(it) { success ->
        val name = success.data.name
        taskName.text = name
        toolbar.title = name
      }
    })

    val id = intent.getLongExtra(TASK_ID, 0)
    lifecycleScope.launchWhenCreated {
      viewModel.loadTask(id)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }
}
