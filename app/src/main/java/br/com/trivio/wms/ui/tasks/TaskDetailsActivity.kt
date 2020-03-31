package br.com.trivio.wms.ui.tasks

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
import br.com.trivio.wms.*
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.data.model.TaskType
import br.com.trivio.wms.ui.conference.cargo.CargoConferenceActivity
import kotlinx.android.synthetic.main.app_bar.*

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
  }

  private val viewModel: TaskDetailsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    setupToolbar()

    val labelTaskStatus = findViewById<TextView>(R.id.label_task_status)
    val btnTaskAction = findViewById<Button>(R.id.btn_task_action)
    val taskName = findViewById<TextView>(R.id.task_name)
    val executorsLabel = findViewById<TextView>(R.id.executors_label)

    viewModel.task.observe(this, Observer {
      threatResult(
        it, always = {
          endLoading()
        },
        onError = {
          finish()
        },
        onSuccess = { success ->
          val task = success.data
          val name = task.name
          toolbar.title = getString(R.string.task) + " " + task.id
          UiUtils.setTaskStatusStyle(labelTaskStatus, task)
          taskName.text = name
          btnTaskAction.text = getActionTextFromTask(task)
          executorsLabel.text = task.currentExecutorsNames
        }
      )
    })

    btnTaskAction.setOnClickListener {
      getIntentFromTask(viewModel.task)?.let {
        startActivity(it)
      }

    }

    val id = intent.getLongExtra(TASK_ID, 0)
    lifecycleScope.launchWhenCreated {
      loadTask(id)
    }
  }

  private fun loadTask(id: Long) {
    startLoading()
    viewModel.loadTask(id)
  }

  private fun getIntentFromTask(
    task: MutableLiveData<Result<TaskDto>>
  ): Intent? {
    return when (val taskResult = task.value) {
      is Result.Success -> {
        val task = taskResult.data
        when (task.type) {
          TaskType.CARGO_CONFERENCE -> {
            val intent = Intent(this, CargoConferenceActivity::class.java)
            intent.putExtra(CargoConferenceActivity.CARGO_ID, task.id)
            intent
          }
          else -> null
        }
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
