package br.com.trivio.wms.data.dto

import br.com.trivio.wms.extensions.matchFilter
import org.joda.time.LocalDateTime

class PickingListDto(
  var id: Long? = null,
  var orderNumber: Long? = null,
  var customerName: String? = null,
  var cargoNumber: String? = null,
  var orderDate: LocalDateTime? = null,
  var status: StatusDto? = null,
  val priorityStatus: StatusDto? = null,
  val quantityItems: Int = 0,
  val progress: Int = 0
) {


  fun search(search: String): Boolean {
    return matchFilter(this.toString(), search)
  }

  override fun toString(): String {
    return "PickingListDto(" +
      "id=$id, " +
      "orderNumber=$orderNumber, " +
      "customerName=$customerName, " +
      "cargoNumber=$cargoNumber, " +
      "orderDate=$orderDate, " +
      "status=$status" +
      ")"
  }
}
