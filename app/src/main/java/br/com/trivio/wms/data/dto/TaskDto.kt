package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskType
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDateTime

data class TaskDto(
  var id: Long? = null,
  var orderReferenceCode: String? = null,
  var name: String? = null,
  //var status: TaskStatus? = null,
  var type: TaskType? = null,
  var currentExecutorsNames: String? = null,
  var createdAt: LocalDateTime? = null,
  var finishedAt: LocalDateTime? = null
)
