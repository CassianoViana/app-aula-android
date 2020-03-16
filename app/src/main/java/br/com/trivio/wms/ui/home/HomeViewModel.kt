package br.com.trivio.wms.ui.home

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.globalData
import br.com.trivio.wms.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val tasksRepository: TasksRepository) : ViewModel() {

  val tasks = MutableLiveData<List<TaskDto>>()

  fun loadTasks() {
    viewModelScope.launch {
      tasks.apply {
        value = withContext(Dispatchers.IO) {
          globalData.userDetails?.let {
            tasksRepository.loadTasks(it.id)
          }
        }
      }
    }
  }
}
