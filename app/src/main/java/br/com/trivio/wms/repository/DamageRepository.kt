package br.com.trivio.wms.repository

import br.com.trivio.wms.serverBackend
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.DamageDto

class DamageRepository {
  fun registerDamage(damageDto: DamageDto): Result<DamageDto> {
    return try {
      serverBackend.registerDamage(damageDto)
      Result.Success(damageDto)
    } catch (e: Exception) {
      Result.Error(e)
    }
  }
}
