package br.com.trivio.wms.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.extensions.isVerySimilar
import br.com.trivio.wms.extensions.similarity
import br.com.trivio.wms.repository.EquipmentsRepository
import br.com.trivio.wms.repository.PickingRepository
import kotlinx.coroutines.launch

class EquipmentsViewModel(
  private val equipmentRepository: EquipmentsRepository = EquipmentsRepository(),
  private val pickingRepository: PickingRepository = PickingRepository()
) :
  ViewModel() {

  val availableEquipments = MutableLiveData<Result<List<EquipmentDto>>>()
  val idsEquipmentsToAdd = MutableLiveData<MutableList<Long>>()

  init {
    idsEquipmentsToAdd.value = mutableListOf()
  }

  fun loadEquipments(taskId: Long) {
    viewModelScope.launch {
      availableEquipments.value = asyncRequest { equipmentRepository.getEquipments(taskId) }
    }
  }

  fun filter(filterString: String): List<EquipmentDto> {
    return when (val value = availableEquipments.value) {
      is Result.Success -> value.data.filter {
        it.toString().isVerySimilar(filterString)
      }.sortedByDescending {
        it.toString().similarity(filterString)
      }
      else -> listOf()
    }
  }

  fun removeEquipment(
    equipment: EquipmentDto,
    taskId: Long,
    callback: (Result<EquipmentDto>) -> Unit
  ) {
    viewModelScope.launch {
      val resultRemoveEquipment = equipmentRepository.removeEquipment(equipment.id, taskId)
      callback(resultRemoveEquipment)
    }
  }

  fun toggleAddEquipment(
    equipment: EquipmentDto
  ) {
    equipment.selected = !equipment.selected
    val equipmentId = equipment.id
    idsEquipmentsToAdd.value = idsEquipmentsToAdd.value?.apply {
      if (this.contains(equipmentId)) {
        this.remove(equipmentId)
      } else {
        this.add(equipmentId)
      }
    }
  }

  fun addEquipmentsByIds(
    equipmentsIds: List<Long>,
    taskId: Long,
    callback: (Result<LongArray>) -> Unit
  ) {
    viewModelScope.launch {
      val successfullyAddedIds =
        equipmentRepository.addEquipments(equipmentsIds.toLongArray(), taskId)
      callback(successfullyAddedIds)
    }
  }
}
