package br.com.trivio.wms.ui.conference.cargo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.dto.DamageDto
import br.com.trivio.wms.data.dto.TaskStatusDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.repository.CargoConferenceRepository
import br.com.trivio.wms.repository.DamageRepository
import kotlinx.coroutines.launch
import java.math.BigDecimal

class CargoConferenceViewModel(
  private val cargoConferenceRepository: CargoConferenceRepository = CargoConferenceRepository(),
  private val damageRepository: DamageRepository = DamageRepository()
) :
  ViewModel() {

  val task = MutableLiveData<Result<CargoConferenceDto>>()
  val cargoItem = MutableLiveData<Result<CargoConferenceItemDto>>()
  val finishStatus = MutableLiveData<Result<Boolean>>()
  val damageRegistration = MutableLiveData<Result<DamageDto>>()

  fun loadCargoConferenceTask(id: Long) {
    viewModelScope.launch {
      task.value = asyncRequest {
        cargoConferenceRepository.loadCargoConference(id)
      }
    }
  }

  fun registerDamage(damageDto: DamageDto) {
    viewModelScope.launch {
      damageRegistration.value = asyncRequest {
        damageRepository.registerDamage(damageDto)
      }
    }
  }

  fun countItem(
    item: CargoConferenceItemDto,
    quantity: BigDecimal
  ) {
    item.countedQuantity = quantity
    viewModelScope.launch {
      cargoItem.value = asyncRequest {
        cargoConferenceRepository.countItem(item)
      }
    }
  }

  fun getCargoItem(gtin: String?): Result<CargoConferenceItemDto> {
    val value: Result<CargoConferenceDto>? = task.value
    return if (value is Result.Success) {
      val item = value.data.items
        .filter { it.gtin != null }
        .firstOrNull {
        it.gtin == gtin
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

  fun filter(search: String): List<CargoConferenceItemDto> {
    return when (val value: Result<CargoConferenceDto>? = task.value) {
      is Result.Success -> value.data.filteredItems(search)
      else -> listOf()
    }
  }

  fun startCounting(taskId: Long, callback: (Result<TaskStatusDto>) -> Unit = {}) {
    viewModelScope.launch {
      val result = asyncRequest { cargoConferenceRepository.startConference(taskId) }
      callback(result)
    }
  }

  fun finishCounting(taskId: Long, callback: (Result<TaskStatusDto>) -> Unit) {
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
}
