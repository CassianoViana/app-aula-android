package br.com.trivio.wms.ui.tasks

import UiUtils
import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.data.model.TaskType
import br.com.trivio.wms.extensions.endLoading
import br.com.trivio.wms.extensions.setupToolbar
import br.com.trivio.wms.extensions.startLoading
import br.com.trivio.wms.threatResult
import br.com.trivio.wms.ui.conference.cargo.CargoConferenceActivity
import kotlinx.android.synthetic.main.activity_task_details.*
import kotlinx.android.synthetic.main.custom_top_bar.*

class TaskDetailsActivity : MyAppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
    const val RESULT_TASK_CHANGED = 100
    const val REQUEST_TASK_CHANGE = 100
  }

  private val viewModel: TaskDetailsViewModel by viewModels()
  private var taskId: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    setupToolbar()
    this.taskId = intent.getLongExtra(TASK_ID, 0)
    observeViewModel()
    onClickBtnStartTask()
    loadTask(taskId)
  }

  private fun observeViewModel() {
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
          updateUi(task)
        }
      )
    })
  }

  private fun onClickBtnStartTask() {
    btn_task_action.setOnClickListener {
      getIntentFromTask(viewModel.task)?.let {
        startActivityForResult(it, REQUEST_TASK_CHANGE)
      }
    }
  }

  private fun updateUi(task: TaskDto) {
    val name = task.name
    title_text_view.text = getString(R.string.task_and_number, task.id)
    UiUtils.setTaskStatusStyle(label_task_status, task)
    task_name.text = name
    btn_task_action.text = getActionTextFromTask(task)
    executors_label.text = task.currentExecutorsNames
    task_hint.text = task.hint
  }

  private fun loadTask(id: Long) {
    startLoading(R.string.loading_task)
    viewModel.loadTask(id)
  }

  private fun getIntentFromTask(
    task: MutableLiveData<Result<TaskDto>>
  ): Intent? {
    return when (val taskResult = task.value) {
      is Result.Success -> {
        val taskRetrieved = taskResult.data
        when (taskRetrieved.type) {
          TaskType.CARGO_CONFERENCE -> {
            val intent = Intent(this, CargoConferenceActivity::class.java)
            intent.putExtra(CargoConferenceActivity.CARGO_TASK_ID, taskRetrieved.id)
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == RESULT_TASK_CHANGED) {
      loadTask(taskId)
    }
    super.onActivityResult(requestCode, resultCode, data)
  }
}
