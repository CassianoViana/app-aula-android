package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.serverBackend

class EquipmentsRepository {

  fun getEquipments(taskId: Long) = Result.call {
    serverBackend.getEquipments(taskId)
  }

  fun addEquipments(ids: LongArray, taskId: Long): Result<LongArray> = Result.call {
    serverBackend.addEquipments(ids, taskId)
  }

  fun removeEquipment(id: Long, taskId: Long): Result<EquipmentDto> = Result.call {
    serverBackend.removeEquipment(id, taskId)
  }
}
