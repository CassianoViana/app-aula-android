package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class CargosRepository {

  fun loadCargos() =
    Result.call { serverBackend.getCargos() }

  fun loadCargosByStatus(status:String) =
    Result.call { serverBackend.getCargosByStatus(status) }

  fun loadCargoConferenceTaskWithoutItems(taskId: Long) =
    Result.call { serverBackend.getCargoConferenceTaskWithoutItems(taskId) }

  fun loadCargoConferenceTask(taskId: Long) =
    Result.call { serverBackend.getCargoConferenceTask(taskId) }

}
