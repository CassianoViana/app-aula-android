package br.com.trivio.wms.repository

import br.com.trivio.wms.backend
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.TaskStatus

class TasksRepository {
  fun loadTasks(userId: Long): Result<List<TaskDto>> {
    return try {
      Result.Success(backend.getTasksByUser(userId))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

  fun loadTask(id: Long): Result<TaskDto> {
    return try {
      Result.Success(backend.getTask(id))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

  fun finishTask(taskId: Long): Result<Boolean> {
    return try {
      backend.finishTask(taskId)
      Result.Success(true)
    } catch (e: Exception) {
      Result.Error(e)
    }
  }
}
