package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.serverBackend
import java.math.BigDecimal

class CargoConferenceRepository {
  fun loadCargoConference(id: Long) =
    Result.call { serverBackend.getCargoConferenceTask(id) }

  fun countItem(
    cargoItem: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String? = null
  ) = Result.call {
    serverBackend.countCargoItem(cargoItem, quantity, description)
    cargoItem
  }

  fun startConference(taskId: Long) = Result.call { serverBackend.startCargoConference(taskId) }
  fun finishConference(taskId: Long) = Result.call { serverBackend.finishCargoConference(taskId) }
  fun restartConference(taskId: Long) = Result.call { serverBackend.restartCargoConference(taskId) }
  fun loadCountsHistory(taskId: Long) = Result.call { serverBackend.loadCountsHistory(taskId) }
  fun undoCountItem(conferenceCountHistoryItemId: Long?) = Result.call { serverBackend.undoCountHistoryItem(conferenceCountHistoryItemId) }
}


