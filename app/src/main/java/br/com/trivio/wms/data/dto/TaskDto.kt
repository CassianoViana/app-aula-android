package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.data.model.TaskType
import org.joda.time.LocalDateTime

data class TaskDto(
  var id: Long = 0,
  var orderReferenceCode: String? = null,
  var name: String? = null,
  var status: TaskStatus? = null,
  var statusDto: TaskStatusDto? = null,
  var type: TaskType? = null,
  var typeDto: TaskTypeDto? = null,
  var currentExecutorsNames: String? = null,
  var createdAt: LocalDateTime? = null,
  var finishedAt: LocalDateTime? = null,
  var hint: String? = null
)
