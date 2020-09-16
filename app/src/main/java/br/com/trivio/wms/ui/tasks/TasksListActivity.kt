package br.com.trivio.wms.ui.tasks

import UiUtils
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.onResult
import kotlinx.android.synthetic.main.activity_tasks.*

class TasksListActivity : MyAppCompatActivity() {

  private val viewModel: TasksViewModel by viewModels()

  private val adapter = TasksAdapter(object : OnTaskClickListener {
    override fun onClick(task: TaskDto) {
      val intent = Intent(this@TasksListActivity, TaskDetailsActivity::class.java)
      intent.putExtra(TaskDetailsActivity.TASK_ID, task.id)
      startActivity(intent)
    }
  })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_tasks)
    observeViewModel()
    bindListAdapter()
    addRefreshListener()
  }

  override fun onResume() {
    super.onResume()
    loadTasks()
  }

  private fun bindListAdapter() {
    tasks_list.setAdapter(adapter)
  }

  private fun addRefreshListener() {
    tasks_list.setOnRefreshListener {
      viewModel.loadTasks()
    }
  }

  private fun observeViewModel() {
    viewModel.tasksResult.observe(this, Observer {
      onResult(it,
        onSuccess = { success ->
          adapter.tasks = success.data
        },
        always = {
          endLoading()
          tasks_list.stopRefresh()
        }
      )
    })
  }

  private fun loadTasks() {
    startLoading()
    viewModel.loadTasks()
  }

  class TasksAdapter(private val onTaskClickListener: OnTaskClickListener) :
    RecyclerView.Adapter<TasksAdapter.ViewHolder>() {

    var tasks: List<TaskDto> = mutableListOf()
      set(value) {
        field = value.sortedByDescending { task -> task.createdAt }
        notifyDataSetChanged()
      }

    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout) {
      private var taskNameText: TextView = layout.findViewById(R.id.ped_cli_txt_view)
      private var taskRefCode: TextView = layout.findViewById(R.id.cargo_txt_view)
      private var taskStatus: TextView = layout.findViewById(R.id.task_status)
      private var taskDate: TextView = layout.findViewById(R.id.item_date)

      fun bind(
        taskDto: TaskDto,
        onTaskClickListener: OnTaskClickListener
      ) {
        taskNameText.text = taskDto.name
        UiUtils.setTaskStatusStyle(taskStatus, taskDto)
        taskRefCode.text = taskDto.id.toString()
        taskDate.text = taskDto.createdAt?.formatTo("dd/MM/yyyy HH:mm")
        layout.setOnClickListener {
          onTaskClickListener.onClick(taskDto)
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(parent.inflateToViewHolder(R.layout.item_task_layout))
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(tasks[position], onTaskClickListener)
    }
  }

  interface OnTaskClickListener {
    fun onClick(task: TaskDto)
  }
}
