package br.com.trivio.wms.ui.cargos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.repository.CargosRepository
import kotlinx.coroutines.launch

class CargoDetailsViewModel(
  private val cargosRepository: CargosRepository = CargosRepository()
) :
  ViewModel() {

  val cargoResult = MutableLiveData<Result<CargoConferenceDto>>()

  fun loadCargo(cargoId: Long, fetchItems: Boolean = false) {
    viewModelScope.launch {
      cargoResult.apply {
        value = asyncRequest {
          when {
            fetchItems -> cargosRepository.loadCargoConferenceTask(cargoId)
            else -> cargosRepository.loadCargoConferenceTaskWithoutItems(cargoId)
          }
        }
      }
    }
  }
}
