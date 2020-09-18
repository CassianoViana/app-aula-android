package br.com.trivio.wms.data.dto

import java.math.BigDecimal

class PickStockPositionDto(
  var name: String = "",
  var type: String = "",
  var qtdItems: BigDecimal = BigDecimal.ZERO,
  var unity: String = "",
)
