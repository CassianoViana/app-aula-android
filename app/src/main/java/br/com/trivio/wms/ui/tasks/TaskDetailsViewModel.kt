package br.com.trivio.wms.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TaskDetailsViewModel(private val tasksRepository: TasksRepository = TasksRepository()) :
  ViewModel() {

  val task = MutableLiveData<Result<TaskDto>>()

  fun loadTask(id: Long) {
    viewModelScope.launch {
      task.apply {
        value = withContext(Dispatchers.IO) {
          tasksRepository.loadTask(id)
        }
      }
    }
  }
}
