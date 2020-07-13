package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.serverBackend

class CargosRepository {

  fun loadPendingCargos() =
    Result.call { serverBackend.getPendingCargos() }

  fun loadCargoDetailsById(cargoId: Long) =
    Result.call { serverBackend.getCargoConferenceDetails(cargoId) }

  fun loadCargoById(id: Long) = Result.call { serverBackend.getCargoConference(id) }

}
