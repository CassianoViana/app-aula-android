package br.com.trivio.wms.data.dto

import br.com.trivio.wms.extensions.formatTo
import br.com.trivio.wms.extensions.matchFilter
import org.joda.time.LocalDateTime

class CargoListDto(
  val ownerName: String = "",
  val shippingCompanyName: String = "",
  val referenceCode: String = "",
  val id: Long = 0,
  val driverName: String = "",
  val truckLabel: String = "",
  val scheduledStart: LocalDateTime? = LocalDateTime(),
  val scheduledEnd: LocalDateTime? = null,
  val taskId: Long = 0,
  val cargoStatusDto: StatusDto? = null,
  val quantityItemsToCount: Int = 0,
  val progress: Int = 0
) {


  override fun toString(): String {
    return "PLACA: $truckLabel - $shippingCompanyName"
  }

  fun bonoName(): String {
    return "BONO: $referenceCode"
  }

  fun formattedScheduledStart(): String {
    return scheduledStart?.formatTo("dd/MM/yyyy HH:mm").toString()
  }

  fun search(filterString: String): Boolean {
    return matchFilter(
      "$truckLabel, $referenceCode, $shippingCompanyName, $quantityItemsToCount, ${this.formattedScheduledStart()}",
      filterString
    );
  }
}
