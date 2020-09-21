package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.matchFilter
import org.joda.time.LocalDateTime

class PickingTaskDto(
  var id: Long = 0L,
  var orderNumber: Long? = null,
  var customerName: String? = null,
  var cargoNumber: String? = null,
  var orderDate: LocalDateTime? = null,
  var taskStatus: TaskStatus? = null,
  val priorityStatus: StatusDto? = null,
  val quantityItems: Int = 0,
  val sellerName: String? = null,
  val sellerCode: String? = null,
  val progress: Int = 0,
  val quantityPickedItems: Int = 0,
  val quantityItemsToPick: Int = 0,
  val quantityPartiallyPicked: Int = 0,
  val items: List<PickingItemDto> = listOf(),
  val equipments: List<EquipmentDto> = listOf()
) {

  val quantityNotFound = this.quantityItemsToPick - this.quantityPickedItems

  companion object {
    const val STATUS_PICKING_ALL_PICKED = 0
    const val STATUS_PICKING_NONE_PICKED = 1
    const val STATUS_PICKING_SOME_PICKED = 2
  }


  fun search(search: String): Boolean {
    return matchFilter(this.toString(), search)
  }

  override fun toString(): String {
    return "PickingTaskDto(" +
      "id=$id, " +
      "orderNumber=$orderNumber, " +
      "customerName=$customerName, " +
      "cargoNumber=$cargoNumber, " +
      "orderDate=$orderDate, " +
      "status=$taskStatus" +
      ")"
  }

  fun isPending() = taskStatus == TaskStatus.PENDING

  fun filteredItems(search: String): List<PickingItemDto> {
    return if (search.isEmpty()) listOf() else items.filter {
      matchFilter(it.getSearchString(), search)
    }
  }

  fun getStatusPicking() = when {
    progress == 100 -> STATUS_PICKING_ALL_PICKED
    progress > 0 -> STATUS_PICKING_SOME_PICKED
    else -> STATUS_PICKING_NONE_PICKED
  }

  fun valid(): Boolean {
    return quantityPartiallyPicked == 0 && quantityNotFound == 0
  }
}

