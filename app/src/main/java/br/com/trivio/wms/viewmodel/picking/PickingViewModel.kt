package br.com.trivio.wms.viewmodel.picking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.PickingItemDto
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.data.dto.StatusDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.extensions.isVerySimilar
import br.com.trivio.wms.extensions.matchRemovingDots
import br.com.trivio.wms.repository.PickingRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

class PickingViewModel(
  private val pickingRepository: PickingRepository = PickingRepository(),
) :
  ViewModel() {

  val task = MutableLiveData<Result<PickingTaskDto>>()
  val pickingItem = MutableLiveData<Result<PickingItemDto>>()
  val items = MutableLiveData<Result<List<PickingItemDto>>>()
  /*

  val finishStatus = MutableLiveData<Result<Boolean>>()
  val countsHistoryList = MutableLiveData<Result<List<ConferenceCountDto>>>()
  */


  fun loadPickingTask(id: Long, callback: (Result<PickingTaskDto>) -> Unit = {}) {
    viewModelScope.launch {
      val result = asyncRequest {
        pickingRepository.loadPickingTask(id)
      }
      task.value = result
      callback(result)
    }
  }

  fun startPicking(taskId: Long, callback: (Result<StatusDto>) -> Unit = {}) {
    viewModelScope.launch {
      val result = asyncRequest { pickingRepository.startPicking(taskId) }
      callback(result)
    }
  }

  fun filterPickingItems(search: String) {
    this.items.value = Result.Success(
      when (val value: Result<PickingTaskDto>? = task.value) {
        is Result.Success -> value.data.filteredItems(search)
        else -> listOf()
      }
    )
  }

  fun getPickingItem(search: String): Result<PickingItemDto> {
    val value: Result<PickingTaskDto>? = task.value
    return if (value is Result.Success) {
      val item = value.data.items
        .firstOrNull {
          it.sku == search ||
            it.gtin == search ||
            (it.name ?: "").isVerySimilar(search) ||
            it.position.isVerySimilar(search)
        }
      if (item == null) {
        Result.Null(item)
      } else {
        Result.Success(item)
      }
    } else {
      Result.Error(IllegalStateException("Não há tarefa de conferẽncia com estado válido"))
    }
  }

  fun devolveItem(
    item: PickingItemDto,
    quantity: BigDecimal,
    callback: (Result<PickingItemDto>) -> Unit
  ) {
    pickItem(
      item = item,
      position = item.position,
      quantity = quantity.multiply(BigDecimal(-1)),
      callback = callback
    )
  }

  fun pickItem(
    item: PickingItemDto,
    position: String = "",
    quantity: BigDecimal,
    callback: (Result<PickingItemDto>) -> Unit
  ) {
    viewModelScope.launch {
      pickingItem.value = asyncRequest {
        pickingRepository.pickItem(item, position, quantity)
      }.apply {
        callback(this)
      }
    }
  }

  fun validatePosition(
    item: PickingItemDto,
    position: String,
    callback: (Result<PickingItemDto>) -> Unit
  ) {
    viewModelScope.launch {
      val pickTask = task.value
      var result: Result<PickingItemDto> =
        Result.Error(java.lang.IllegalStateException("Posição não encontrada"))
      if (pickTask is Result.Success) {
        if (item.position.matchRemovingDots(position)) {
          result = Result.Success(item)
        }
      }
      callback(result)
    }
  }

  fun validateProduct(
    item: PickingItemDto,
    code: String,
    callback: (Result<PickingItemDto>) -> Unit
  ) {
    viewModelScope.launch {
      val pickTask = task.value
      var result: Result<PickingItemDto> =
        Result.Error(java.lang.IllegalStateException("Produto não encontrada"))
      if (pickTask is Result.Success) {
        if (item.matchCode(code)) {
          result = Result.Success(item)
        }
      }
      callback(result)
    }
  }

  fun findNextItem(pickedItem: PickingItemDto, callback: (Result<PickingItemDto>) -> Unit) {
    val pickTask = task.value
    var result: Result<PickingItemDto> =
      Result.Error(IllegalArgumentException("Item não encontrado"))
    if (pickTask is Result.Success) {
      val pickItems = pickTask.data.items
      pickItems.firstOrNull() { it.order == pickedItem.order + 1 }.let {
        if (it != null) {
          result = Result.Success(it)
        } else {
          result = Result.Null(it)
        }
      }
    }
    callback(result)
  }

  fun finishPicking(taskId: Long, callback: (Result<StatusDto>) -> Unit) {
    viewModelScope.launch {
      val status = asyncRequest {
        pickingRepository.finishPicking(taskId)
      }
      callback(status)
    }
  }

  /*
  fun filterConferenceItems(search: String) {
    this.items.value = Result.Success(
      when (val value: Result<CargoConferenceDto>? = task.value) {
        is Result.Success -> value.data.filteredItems(search)
        else -> listOf()
      }
    )
  }

  fun filterCountHistory(search: String): List<ConferenceCountDto> {
    val history = countsHistoryList.value
    return when (history) {
      is Result.Success -> history.data.filter {
        search.isEmpty() || it.gtin == search || it.sku == search || it.product?.isVerySimilar(
          search
        ) ?: false
      }
      else -> listOf()
    }
  }





  fun finishCounting(taskId: Long, callback: (Result<StatusDto>) -> Unit) {
    viewModelScope.launch {
      val result = asyncRequest { pickingRepository.finishConference(taskId) }
      callback(result)
    }
  }

  fun restartCounting(taskId: Long, callback: (Result<CargoConferenceDto>) -> Unit = {}) {
    viewModelScope.launch {
      asyncRequest {
        pickingRepository.restartConference(taskId)
      }.let { restartedCountingTask ->
        task.value = restartedCountingTask
        callback(restartedCountingTask)
      }
    }
  }

  fun loadCountHistory(taskId: Long) {
    viewModelScope.launch {
      countsHistoryList.value = asyncRequest {
        pickingRepository.loadCountsHistory(taskId)
      }
    }
  }

  fun undoCount(conferenceCountDto: ConferenceCountDto, callback: (Result<Any>) -> Any) {
    viewModelScope.launch {
      asyncRequest {
        pickingRepository.undoCountItem(conferenceCountDto.id)
      }.let {
        callback(it)
      }
    }
  }*/
}
