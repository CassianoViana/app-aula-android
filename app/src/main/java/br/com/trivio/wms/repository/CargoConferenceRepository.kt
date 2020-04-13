package br.com.trivio.wms.repository

import br.com.trivio.wms.backend
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto

class CargoConferenceRepository {
  fun loadCargoConference(id: Long): Result<CargoConferenceDto> {
    return try {
      Result.Success(backend.getCargoConference(id))
    } catch (e: Exception) {
      Result.Error(e)
    }
  }

  fun countItem(cargoItem: CargoConferenceItemDto): Result<CargoConferenceItemDto> {
    return try {
      backend.countCargoItem(cargoItem)
      Result.Success(cargoItem)
    } catch (e: Exception) {
      Result.Error(e);
    }
  }
}
