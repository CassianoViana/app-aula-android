package br.com.trivio.wms.data.dto

import org.joda.time.LocalDateTime
import java.math.BigDecimal

class ConferenceCountDto(
  var id:Long? = 0,
  var created: LocalDateTime? = null,
  var count: BigDecimal? = null,
  var currentCountedQuantity: BigDecimal? = null,
  var currentDamagedQuantity: BigDecimal? = null,
  var description: String? = null,
  var gtin: String? = null,
  var sku: String? = null,
  var product: String? = null,
  var username: String? = null,
  var countType: CountTypeDto? = null,
  var storageUnitCode: String? = null
)
