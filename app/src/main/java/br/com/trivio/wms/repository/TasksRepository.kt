package br.com.trivio.wms.repository

import br.com.trivio.wms.serverBackend
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto

class TasksRepository {
  fun loadTasks(userId: Long): Result<List<TaskDto>> {
    return try {
      Result.Success(serverBackend.getTasksByUser(userId))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

  fun loadTask(id: Long): Result<TaskDto> {
    return try {
      Result.Success(serverBackend.getTask(id))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

  fun finishTask(taskId: Long): Result<Boolean> {
    return try {
      serverBackend.finishTask(taskId)
      Result.Success(true)
    } catch (e: Exception) {
      Result.Error(e)
    }
  }
}
