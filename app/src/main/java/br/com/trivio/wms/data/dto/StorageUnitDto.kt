package br.com.trivio.wms.data.dto

import org.joda.time.LocalDateTime
import java.math.BigDecimal

class StorageUnitDto(
  var id: Long? = null,
  var referenceCode: String? = null,
  var integrationDate: LocalDateTime? = null,
  var code: String? = null,
  var description: String? = null,
  var defaultFactor: BigDecimal? = null,
  var decimals: BigDecimal? = null
)
