package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class EquipmentsRepository {

  fun getEquipments(taskId: Long) = Result.call {
    serverBackend.getEquipments(taskId)
  }

  fun addEquipments(ids: List<Long>, taskId: Long): Result<List<Long>> = Result.call {
    serverBackend.addEquipments(ids, taskId)
  }

  fun removeEquipment(id: Long): Result<Any> = Result.call {
    serverBackend.removeEquipment(id)
  }
}
