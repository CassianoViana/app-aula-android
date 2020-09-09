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

  fun isCountedWithDivergences(): Boolean {
    return isCounted() && !isCountedCorrectly()
  }

  fun isCountedCorrectly(): Boolean {
    return isCounted() && expectedQuantity == countedQuantity
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

  fun count(quantity: BigDecimal) {
    this.countedQuantity = this.countedQuantity?.add(quantity)
  }

  fun isCounted(): Boolean {
    return countedQuantity != null
  }
}
