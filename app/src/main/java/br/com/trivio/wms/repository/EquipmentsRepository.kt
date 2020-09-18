package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class EquipmentsRepository {

  fun getEquipments(taskId: Long) =
    Result.call { serverBackend.getEquipments(taskId) }

}