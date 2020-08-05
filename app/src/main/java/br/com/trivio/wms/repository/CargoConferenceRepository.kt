package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.serverBackend

class CargoConferenceRepository {
  fun loadCargoConference(id: Long) =
    Result.call { serverBackend.getCargoConferenceTask(id) }

  fun countItem(cargoItem: CargoConferenceItemDto) = Result.call {
    serverBackend.countCargoItem(cargoItem)
    cargoItem
  }

  fun startConference(taskId: Long) = Result.call { serverBackend.startCargoConference(taskId) }
  fun finishConference(taskId: Long) = Result.call { serverBackend.finishCargoConference(taskId) }
  fun restartConference(taskId: Long) = Result.call { serverBackend.restartCargoConference(taskId) }
}


