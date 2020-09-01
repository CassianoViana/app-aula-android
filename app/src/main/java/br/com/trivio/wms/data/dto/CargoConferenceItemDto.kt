package br.com.trivio.wms.data.dto

import java.math.BigDecimal

data class CargoConferenceItemDto(
  val id: Long = 0,
  val cargoItemId: Long = 0,
  val name: String = "",
  val gtin: String = "",
  val sku: String = "",
  val expectedQuantity: BigDecimal? = BigDecimal("0"),
  var countedQuantity: BigDecimal? = BigDecimal("0"),
  var damage: DamageDto? = null,
  var storageUnit: StorageUnitDto? = null
) {
  fun getSearchString(): String {
    return "$name, $gtin, $sku, $expectedQuantity, $countedQuantity"
  }

  fun mismatchQuantity(): Boolean {
    return countedQuantity != null && !correctCounted()
  }

  fun correctCounted(): Boolean {
    return countedQuantity != null && expectedQuantity == countedQuantity
  }

  override fun equals(other: Any?): Boolean {
    if (other is CargoConferenceItemDto) {
      return id == other.id
    }
    return false
  }

  override fun hashCode(): Int {
    return id.hashCode()
  }
}
