package br.com.trivio.wms.repository

import br.com.trivio.wms.backend
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.TaskDto

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
}
