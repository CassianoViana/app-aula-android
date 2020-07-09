package br.com.trivio.wms.ui.tasks

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.extensions.callAsync
import br.com.trivio.wms.globalData
import br.com.trivio.wms.repository.TasksRepository
import kotlinx.coroutines.launch

class TasksViewModel(
  private val tasksRepository: TasksRepository = TasksRepository()
) :
  ViewModel() {

  val tasksResult = MutableLiveData<Result<List<TaskDto>>>()

  fun loadTasks() {
    viewModelScope.launch {
      tasksResult.apply {
        value = callAsync {
          globalData.userDetails?.let {
            tasksRepository.loadTasks(it.id)
          }
        }
      }
    }
  }
}
