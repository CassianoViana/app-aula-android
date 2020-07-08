package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.serverBackend

class CargosRepository {

  fun loadCargoArrivals(): Result<List<CargoListDto>> {
    return try {
      Result.Success(serverBackend.listCargos())
    } catch (e: Exception) {
      Result.Error(e)
    }
  }
}
