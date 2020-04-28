package br.com.trivio.wms.data.dto

import java.math.BigDecimal

data class DamageDto(
  var cargoItemId: Long = 0,
  var quantity: BigDecimal = BigDecimal.ZERO,
  var description: String = ""
)
