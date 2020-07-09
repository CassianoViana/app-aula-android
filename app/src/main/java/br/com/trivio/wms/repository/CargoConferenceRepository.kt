package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.serverBackend

class CargoConferenceRepository {
  fun loadCargoConference(id: Long) = Result.call { serverBackend.getCargoConference(id) }
  fun countItem(cargoItem: CargoConferenceItemDto): Result<CargoConferenceItemDto> {
    return Result.call {
      serverBackend.countCargoItem(cargoItem)
      cargoItem
    }
  }
}
