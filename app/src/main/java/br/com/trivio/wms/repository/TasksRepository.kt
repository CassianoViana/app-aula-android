package br.com.trivio.wms.repository

import br.com.trivio.wms.api
import br.com.trivio.wms.data.dto.TaskDto
import kotlinx.coroutines.delay
import java.util.*

class TasksRepository {
  suspend fun loadTasks(userId: Long): List<TaskDto> {
    return api.getTasksByUser(userId)
  }

}
