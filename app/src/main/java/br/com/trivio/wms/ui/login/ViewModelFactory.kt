package br.com.trivio.wms.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import br.com.trivio.wms.data.LoginDataSource
import br.com.trivio.wms.data.LoginRepository
import br.com.trivio.wms.repository.TasksRepository
import br.com.trivio.wms.ui.tasks.TasksViewModel

class ViewModelFactory : ViewModelProvider.Factory {

  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
      return LoginViewModel(
        loginRepository = LoginRepository(
          dataSource = LoginDataSource()
        )
      ) as T
    }

    if (modelClass.isAssignableFrom(TasksViewModel::class.java)) {
      return TasksViewModel(
        tasksRepository = TasksRepository()
      ) as T
    }

    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
