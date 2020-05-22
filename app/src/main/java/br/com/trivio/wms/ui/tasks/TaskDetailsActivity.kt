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
import kotlinx.coroutines.launch

class TaskDetailsActivity : AppCompatActivity() {

  companion object {
    const val TASK_ID: String = "task_id"
    const val RESULT_TASK_CHANGED = 100
    const val REQUEST_TASK_CHANGE = 100
  }

  private lateinit var taskHint: TextView
  private lateinit var executorsLabel: TextView
  private lateinit var taskName: TextView
  private lateinit var btnTaskAction: Button
  private lateinit var labelTaskStatus: TextView
  private val viewModel: TaskDetailsViewModel by viewModels()
  private var taskId: Long = 0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_task_details)
    setupToolbar()
    associateComponents()
    this.taskId = intent.getLongExtra(TASK_ID, 0)
    observeViewModel()
    onClickBtnStartTask()
    loadTask(taskId)
  }

  private fun associateComponents() {
    labelTaskStatus = findViewById(R.id.label_task_status)
    btnTaskAction = findViewById(R.id.btn_task_action)
    taskName = findViewById(R.id.task_name)
    executorsLabel = findViewById(R.id.executors_label)
    taskHint = findViewById(R.id.task_hint)
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
    btnTaskAction.setOnClickListener {
      getIntentFromTask(viewModel.task)?.let {
        startActivityForResult(it, REQUEST_TASK_CHANGE)
      }
    }
  }

  private fun updateUi(task: TaskDto) {
    val name = task.name
    toolbar.title = getString(R.string.task) + " " + task.id
    UiUtils.setTaskStatusStyle(labelTaskStatus, task)
    taskName.text = name
    btnTaskAction.text = getActionTextFromTask(task)
    executorsLabel.text = task.currentExecutorsNames
    taskHint.text = task.hint
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

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == RESULT_TASK_CHANGED) {
      loadTask(taskId)
    }
    super.onActivityResult(requestCode, resultCode, data)
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }
}
