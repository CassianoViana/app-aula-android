package br.com.trivio.wms.viewmodel.cargo.conference

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.dto.ConferenceCountDto
import br.com.trivio.wms.data.dto.StatusDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.extensions.isVerySimilar
import br.com.trivio.wms.repository.CargoConferenceRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CargoConferenceViewModel(
  private val cargoConferenceRepository: CargoConferenceRepository = CargoConferenceRepository(),
) :
  ViewModel() {

  val task = MutableLiveData<Result<CargoConferenceDto>>()
  val items = MutableLiveData<Result<List<CargoConferenceItemDto>>>()
  val cargoItem = MutableLiveData<Result<CargoConferenceItemDto>>()
  val finishStatus = MutableLiveData<Result<Boolean>>()
  val countsHistoryList = MutableLiveData<Result<List<ConferenceCountDto>>>()

  fun loadCargoConferenceTask(id: Long) {
    viewModelScope.launch {
      task.value = asyncRequest {
        cargoConferenceRepository.loadCargoConference(id)
      }
    }
  }

  fun countItem(
    item: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String? = null
  ) {
    viewModelScope.launch {
      cargoItem.value = asyncRequest {
        cargoConferenceRepository.countItem(item, quantity, description)
      }
    }
  }

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

  fun getCargoItem(search: String): Result<CargoConferenceItemDto> {
    val value: Result<CargoConferenceDto>? = task.value
    return if (value is Result.Success) {
      val item = value.data.items
        .firstOrNull {
          it.sku == search ||
            it.gtin == search ||
            it.name.isVerySimilar(search)
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

  fun startCounting(taskId: Long, callback: (Result<StatusDto>) -> Unit = {}) {
    viewModelScope.launch {
      val result = asyncRequest { cargoConferenceRepository.startConference(taskId) }
      callback(result)
    }
  }

  fun finishCounting(taskId: Long, callback: (Result<StatusDto>) -> Unit) {
    viewModelScope.launch {
      val result = asyncRequest { cargoConferenceRepository.finishConference(taskId) }
      callback(result)
    }
  }

  fun restartCounting(taskId: Long, callback: (Result<CargoConferenceDto>) -> Unit = {}) {
    viewModelScope.launch {
      asyncRequest {
        cargoConferenceRepository.restartConference(taskId)
      }.let { restartedCountingTask ->
        task.value = restartedCountingTask
        callback(restartedCountingTask)
      }
    }
  }

  fun loadCountHistory(taskId: Long) {
    viewModelScope.launch {
      countsHistoryList.value = asyncRequest {
        cargoConferenceRepository.loadCountsHistory(taskId)
      }
    }
  }

  fun undoCount(conferenceCountDto: ConferenceCountDto, callback: (Result<Any>) -> Any) {
    viewModelScope.launch {
      asyncRequest {
        cargoConferenceRepository.undoCountItem(conferenceCountDto.id)
      }.let {
        callback(it)
      }
    }
  }
}
