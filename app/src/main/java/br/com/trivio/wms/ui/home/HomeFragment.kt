package br.com.trivio.wms.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.ui.login.ViewModelFactory

class HomeFragment : Fragment() {

  private lateinit var viewModel: HomeViewModel
  private lateinit var loading: ProgressBar
  private lateinit var tasksList: RecyclerView

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View? {
    val root = inflater.inflate(R.layout.fragment_home, container, false)
    tasksList = root.findViewById(R.id.tasks_recycler_view)
    loading = root.findViewById(R.id.progressBarTasks)
    viewModel = ViewModelProviders.of(this, ViewModelFactory()).get(HomeViewModel::class.java)
    bindDataModel()
    loadTasks()
    return root
  }

  private fun loadTasks() {
    loading.visibility = View.VISIBLE
    viewModel.loadTasks()
  }

  private fun bindDataModel() {
    val adapter = TasksAdapter()
    tasksList.adapter = adapter
    tasksList.layoutManager = LinearLayoutManager(activity)
    viewModel.tasks.observe(this, Observer {
      adapter.tasks = it
      loading.visibility = View.GONE
    })
  }

  class TasksAdapter : RecyclerView.Adapter<TasksAdapter.ViewHolder>() {
    var tasks: List<TaskDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout) {
      private var taskNameText: TextView = layout.findViewById(R.id.task_name_text)
      fun bind(taskDto: TaskDto) {
        taskNameText.text = taskDto.name
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.task_item_layout, parent, false)
      return ViewHolder(view)
    }

    override fun getItemCount() = tasks.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(tasks[position])
    }
  }
}
