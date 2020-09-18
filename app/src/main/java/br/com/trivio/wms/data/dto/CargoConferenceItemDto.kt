package br.com.trivio.wms.data.dto

import br.com.trivio.wms.extensions.coalesce
import java.math.BigDecimal

data class CargoConferenceItemDto(
  val id: Long = 0,
  val cargoItemId: Long = 0,
  val name: String = "",
  val gtin: String? = null,
  val sku: String = "",
  val expectedQuantity: BigDecimal? = BigDecimal.ZERO,
  var countedQuantity: BigDecimal? = BigDecimal.ZERO,
  var damagedQuantity: BigDecimal? = BigDecimal.ZERO,
  var storageUnit: StorageUnitDto? = null,
  var counts: List<ConferenceCountDto> = listOf()
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

  fun getUnitCode(default: String) = coalesce(storageUnit?.code, default)

  fun hasDamagedQtd(): Boolean {
    return damagedQuantity != null && damagedQuantity?.compareTo(
      BigDecimal.ZERO
    ) != 0
  }

  fun addDamageQtd(damagedQtd: BigDecimal) {
    this.countedQuantity = (this.countedQuantity ?: BigDecimal.ZERO).add(damagedQtd)
    this.damagedQuantity = (damagedQuantity ?: BigDecimal.ZERO).add(damagedQtd)
  }
}
