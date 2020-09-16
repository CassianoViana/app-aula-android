package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.matchFilter
import org.joda.time.LocalDateTime

class PickingTaskDto(
  var id: Long? = null,
  var orderNumber: Long? = null,
  var customerName: String? = null,
  var cargoNumber: String? = null,
  var orderDate: LocalDateTime? = null,
  var status: TaskStatus? = null,
  val priorityStatus: StatusDto? = null,
  val quantityItems: Int = 0,
  val sellerName: String? = null,
  val sellerCode: String? = null,
  val quantityPickedItems: Int = 0,
  val quantityItemsToPick: Int = 0,
) {


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
      "status=$status" +
      ")"
  }
}

