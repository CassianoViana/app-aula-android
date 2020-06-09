package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.DamageDto
import br.com.trivio.wms.serverBackend

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
