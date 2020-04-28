package br.com.trivio.wms.ui.conference.cargo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.dto.DamageDto
import br.com.trivio.wms.repository.CargoConferenceRepository
import br.com.trivio.wms.repository.DamageRepository
import br.com.trivio.wms.repository.TasksRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class CargoConferenceViewModel(
  private val repository: CargoConferenceRepository = CargoConferenceRepository(),
  private val damageRepository: DamageRepository = DamageRepository(),
  private val taskRepository: TasksRepository = TasksRepository()
) :
  ViewModel() {

  val task = MutableLiveData<Result<CargoConferenceDto>>()
  val cargoItem = MutableLiveData<Result<CargoConferenceItemDto>>()
  val finishStatus = MutableLiveData<Result<Boolean>>()
  val damageRegistration = MutableLiveData<Result<DamageDto>>()

  fun loadTask(id: Long) {
    viewModelScope.launch {
      task.value = withContext(Dispatchers.IO) {
        repository.loadCargoConference(id)
      }
    }
  }

  fun registerDamage(damageDto: DamageDto) {
    viewModelScope.launch {
      damageRegistration.value = withContext(Dispatchers.IO) {
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
      cargoItem.value = withContext(Dispatchers.IO) {
        repository.countItem(item)
      }
    }
  }

  fun getCargoItem(gtin: String): Result<CargoConferenceItemDto> {
    val value: Result<CargoConferenceDto>? = task.value
    return if (value is Result.Success) {
      val item = value.data.items.firstOrNull {
        it.gtin == gtin
      }
      if (item == null) {
        Result.Error(IllegalStateException("Não foi encontrado o produto com o GTIN $gtin"))
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

  fun finishTask(taskId: Long) {
    viewModelScope.launch {
      finishStatus.value = withContext(Dispatchers.IO) {
        taskRepository.finishTask(taskId)
      }
    }
  }
}
