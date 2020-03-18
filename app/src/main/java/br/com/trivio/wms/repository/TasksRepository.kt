package br.com.trivio.wms.repository

import br.com.trivio.wms.api
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import kotlinx.coroutines.delay
import java.util.*

class TasksRepository {
  fun loadTasks(userId: Long): Result<List<TaskDto>> {
    return try {
      Result.Success(api.getTasksByUser(userId))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

}
