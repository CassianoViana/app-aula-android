package br.com.trivio.wms.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
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
import br.com.trivio.wms.databinding.ActivityTaskDetailsBinding
import br.com.trivio.wms.ui.conference.cargo.CargoConferenceActivity
import kotlinx.android.synthetic.main.app_bar.*

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
  }

  private lateinit var binding: ActivityTaskDetailsBinding
  private val viewModel: TaskDetailsViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityTaskDetailsBinding.inflate(layoutInflater)
    setContentView(R.layout.activity_task_details)
    setupToolbar()

    viewModel.task.observe(this, Observer {
      threatResult(it,
        onSuccess = { success ->
          val task = success.data
          val name = task.name
          toolbar.title = getString(R.string.task) + " " + task.id
          UiUtils.setTaskStatusStyle(binding.labelTaskStatus, task)
          binding.taskName.text = name
          binding.btnTaskAction.text = getActionTextFromTask(task)
          binding.executorsLabel.text = task.currentExecutorsNames
        },
        always = {
          Log.i("endloading", "endloading")
          endLoading()
        },
        onError = {
          finish()
        }
      )
    })

    binding.labelTaskStatus.setOnClickListener {
      startActivity(Intent(this, getActivityClassFromTask(viewModel.task)))
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

  private fun getActivityClassFromTask(task: MutableLiveData<Result<TaskDto>>): Class<*>? {
    return when (val taskResult = task.value) {
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
