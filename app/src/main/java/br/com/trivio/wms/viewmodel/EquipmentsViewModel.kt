package br.com.trivio.wms.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.repository.EquipmentsRepository
import kotlinx.coroutines.launch

class EquipmentsViewModel(
  private val equipmentRepository: EquipmentsRepository = EquipmentsRepository()
) :
  ViewModel() {

  val equipmentsResult = MutableLiveData<Result<List<EquipmentDto>>>()

  fun loadPickings(taskId: Long) {
    viewModelScope.launch {
      equipmentsResult.value = asyncRequest { equipmentRepository.getEquipments(taskId) }
    }
  }

  fun filter(filterString: String): List<EquipmentDto> {
    return when (val value = equipmentsResult.value) {
      is Result.Success -> value.data.filter {
        it.search(filterString)
      }
      else -> listOf()
    }
  }
}
