package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class CargosRepository {

  fun loadCargoArrivals() = Result.call { serverBackend.listCargos() }
  fun loadCargoById(cargoId: Long) = Result.call { serverBackend.getCargoConference(cargoId) }

}
