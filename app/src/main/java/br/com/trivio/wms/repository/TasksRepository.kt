package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class TasksRepository {
  fun loadTasks(userId: Long) = Result.call {
    serverBackend.getTasksByUser(userId)
  }

  fun loadTask(id: Long) = Result.call {
    serverBackend.getTask(id)
  }

  fun finishTask(taskId: Long) = Result.call {
    serverBackend.finishTask(taskId)
    true
  }
}
