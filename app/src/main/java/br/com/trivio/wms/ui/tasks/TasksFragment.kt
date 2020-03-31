package br.com.trivio.wms.ui.tasks

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.*
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.ui.login.ViewModelFactory

class TasksFragment : Fragment() {

  private lateinit var viewModel: TasksViewModel
  private lateinit var loading: ProgressBar
  private lateinit var tasksList: RecyclerView

  private val adapter = TasksAdapter(object : OnTaskClickListener {
    override fun onClick(task: TaskDto) {
      val intent = Intent(activity, TaskDetailsActivity::class.java)
      intent.putExtra(TaskDetailsActivity.TASK_ID, task.id)
      startActivity(intent)
    }
  })

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val root = inflater.inflate(R.layout.fragment_tasks, container, false)
    tasksList = root.findViewById(R.id.tasks_recycler_view)
    loading = root.findViewById(R.id.progress_bar)
    viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(TasksViewModel::class.java)
    bindListAdapter()
    observeViewModel()
    loadTasks()
    return root
  }

  private fun bindListAdapter() {
    val activity = activity as AppCompatActivity
    tasksList.layoutManager = LinearLayoutManager(activity)
    tasksList.adapter = adapter
  }

  private fun observeViewModel() {
    viewModel.tasksResult.observe(this, Observer {
      threatResult(it,
        onSuccess = { success ->
          adapter.tasks = success.data
        },
        always = {
          endLoading()
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
      private var taskNameText: TextView = layout.findViewById(R.id.task_name_text)
      private var taskRefCode: TextView = layout.findViewById(R.id.task_code)
      private var taskStatus: TextView = layout.findViewById(R.id.task_status)
      private var taskDate: TextView = layout.findViewById(R.id.task_date)

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
      ViewHolder(parent.inflateToViewHolder(R.layout.item_task_layout))

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(tasks[position], onTaskClickListener)
    }
  }

  interface OnTaskClickListener {
    fun onClick(task: TaskDto)
  }
}
