package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.matchFilter
import org.joda.time.LocalDateTime

class CargoConferenceDto(
  val id: Long = 0,
  val taskId: Long = 0,
  val items: MutableList<CargoConferenceItemDto> = mutableListOf(),
  val scheduledStart: LocalDateTime? = null,
  val scheduledEnd: LocalDateTime? = null,
  val driverName: String = "",
  val truckLabel: String = "",
  val restartCounter: Int? = 0,
  val providerName: String = "",
  val nfesCompanyNames: List<String> = mutableListOf(),
  val cargoReferenceCode: String = "",
  val quantityItems: Int = 0,
  val totalInvalid: Int = 0,
  val totalValid: Int = 0,
  val taskStatus: TaskStatus = TaskStatus.PENDING,
  val progress: Int = 0
) {

  var totalToCountItems: Int = 0
  var totalCountedItems: Int = 0

  var progressDescription: String? = null
    set(value) {
      field = value
      updateTotalsByProgressDescription()
    }

  private fun updateTotalsByProgressDescription() {
    progressDescription?.split(Regex("[\\D]"))?.let {
      var counted: Int
      var total: Int
      it[0].let {
        counted = it.toInt()
      }
      it[1].let {
        total = it.toInt()
      }
      totalCountedItems = counted
      totalToCountItems = total - counted
    }
  }

  fun filteredItems(search: String): List<CargoConferenceItemDto> {
    if (search.isEmpty()) return items
    return items.filter {
      matchFilter(it.getSearchString(), search)
    }
  }

  companion object {
    const val STATUS_COUNTING_ALL_COUNTED = 0
    const val STATUS_COUNTING_NONE_COUNTED = 1
    const val STATUS_COUNTING_SOME_COUNTED = 2
  }

  fun getTotalDivergentItems() = items.filter { it.isCountedWithDivergences() }.count()
  fun isPending() = taskStatus == TaskStatus.PENDING
  fun getPercentProgress() = progress
  fun isValid(): Boolean = totalInvalid == 0 && totalCountedItems > 0
  fun getStatusCounting() = when {
    progress == 100 -> {
      STATUS_COUNTING_ALL_COUNTED
    }
    progress > 0 -> {
      STATUS_COUNTING_SOME_COUNTED
    }
    else -> {
      STATUS_COUNTING_NONE_COUNTED
    }
  }

}
