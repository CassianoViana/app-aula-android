package br.com.trivio.wms.data.dto

import br.com.trivio.wms.stringSimilarity
import java.math.BigDecimal

data class CargoConferenceItemDto(
  val id: Long = 0,
  val name: String = "",
  val gtin: String = "",
  val sku: String = "",
  val expectedQuantity: BigDecimal? = BigDecimal("0"),
  var countedQuantity: BigDecimal? = BigDecimal("0")
) {
  fun getSearchString(): String {
    return "$name, $gtin, $sku, $expectedQuantity, $countedQuantity"
  }
}
