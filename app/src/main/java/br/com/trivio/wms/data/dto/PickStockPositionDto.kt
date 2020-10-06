package br.com.trivio.wms.data.dto

import java.math.BigDecimal

class PickStockPositionDto(
  var name: String? = null,
  var type: String? = null,
  var qtdItems: BigDecimal? = BigDecimal.ZERO,
  var unity: String? = null,
)
