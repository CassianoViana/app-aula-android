package br.com.trivio.wms.data.dto

import br.com.trivio.wms.extensions.coalesce
import java.math.BigDecimal

class PickingItemDto(
  val id: Long = 0,
  val sku: String = "",
  val gtin: String = "",
  val name: String = "",
  val position: String = "",
  var storageUnit: StorageUnitDto? = null,
  val expectedQuantityToPick: BigDecimal? = BigDecimal.ZERO,
  var pickedQuantity: BigDecimal? = BigDecimal.ZERO,
  var status: StatusDto,
  val stockPositions: List<PickStockPositionDto> = mutableListOf(),
  val hasRequestedPickingReposition: Boolean = false
) {


  fun getSearchString(): String {
    return toString()
  }

  override fun toString(): String {
    return "PickingItemDto(id=$id)"
  }

  fun getUnitCode(default: String = "") = coalesce(storageUnit?.code, default)

  fun isQtdPickedCorrect(): Boolean {
    return hasItemsPicked() && expectedQuantityToPick == pickedQuantity
  }

  fun hasItemsPicked(): Boolean {
    return pickedQuantity != null
  }

}
