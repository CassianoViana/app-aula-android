package br.com.trivio.wms

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.data.model.TaskType
import br.com.trivio.wms.ui.tasks.TaskDetailsViewModel
import kotlinx.android.synthetic.main.app_bar.*

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    val viewModel: TaskDetailsViewModel by viewModels()

    val taskName = findViewById<TextView>(R.id.task_name)
    val taskStatus = findViewById<TextView>(R.id.label_task_status)
    val btnTaskAction = findViewById<Button>(R.id.btn_task_action)

    setupToolbar()
    viewModel.task.observe(this, Observer {
      threatResult(it) { success ->
        val task: TaskDto = success.data
        val name = task.name
        toolbar.title = getString(R.string.task) + " " + task.id
        UiUtils.setTaskStatusStyle(taskStatus, task)
        taskName.text = name
        btnTaskAction.text = getActionTextFromTask(task)
      }
    })

    btnTaskAction.setOnClickListener {
      startActivity(Intent(this, getActivityClassFromTask(viewModel.task)))
    }

    val id = intent.getLongExtra(TASK_ID, 0)
    lifecycleScope.launchWhenCreated {
      viewModel.loadTask(id)
    }
  }

  private fun getActivityClassFromTask(task: MutableLiveData<Result<TaskDto>>): Class<*>? {
    val taskResult: Result<TaskDto>? = task.value
    return when (taskResult) {
      is Result.Success -> when (taskResult.data.type) {
        TaskType.CARGO_CONFERENCE -> CargoConferenceActivity::class.java
        else -> null
      }
      else -> null
    }
  }

  private fun getActionTextFromTask(task: TaskDto): String {
    return when (task.status) {
      TaskStatus.DONE -> getString(R.string.open)
      TaskStatus.PENDING -> getString(R.string.start)
      TaskStatus.CANCELLED -> getString(R.string.open)
      TaskStatus.DOING -> getString(R.string.continue_task)
      else -> getString(R.string.open)
    }
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }
}
