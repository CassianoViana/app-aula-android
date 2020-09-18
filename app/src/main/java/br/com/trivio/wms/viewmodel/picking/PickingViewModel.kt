package br.com.trivio.wms.viewmodel.picking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.PickingItemDto
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.data.dto.TaskStatusDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.extensions.isVerySimilar
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


  fun loadPickingTask(id: Long) {
    viewModelScope.launch {
      task.value = asyncRequest {
        pickingRepository.loadPickingTask(id)
      }
    }
  }

  fun startPicking(taskId: Long, callback: (Result<TaskStatusDto>) -> Unit = {}) {
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
            it.name.isVerySimilar(search) ||
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

  fun pickItem(
    item: PickingItemDto,
    quantity: BigDecimal,
  ) {
    viewModelScope.launch {
      pickingItem.value = asyncRequest {
        pickingRepository.pickItem(item, quantity)
      }
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





  fun finishCounting(taskId: Long, callback: (Result<TaskStatusDto>) -> Unit) {
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
