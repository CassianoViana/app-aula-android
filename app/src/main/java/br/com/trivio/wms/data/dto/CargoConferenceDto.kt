package br.com.trivio.wms.data.dto

import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.getPercent
import br.com.trivio.wms.extensions.matchFilter

class CargoConferenceDto(
  val id: Long = 0,
  val items: MutableList<CargoConferenceItemDto> = mutableListOf(),
  val taskStatus: TaskStatus = TaskStatus.PENDING
) {
  fun getTotalCountedItems(): Int {
    return items.filter {
      it.countedQuantity != null
    }.size
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

  fun getStatusCounting(): Int {
    return when {
      getTotalCountedItems() == items.size -> {
        STATUS_COUNTING_ALL_COUNTED
      }
      getTotalCountedItems() > 0 -> {
        STATUS_COUNTING_SOME_COUNTED
      }
      else -> {
        STATUS_COUNTING_NONE_COUNTED
      }
    }
  }

  fun getPercentProgress(): Int {
    return getPercent(getTotalCountedItems(), items.size)
  }

  fun getTotalDivergentItems(): Int {
    return items.filter {
      it.mismatchQuantity()
    }.count()
  }
}
