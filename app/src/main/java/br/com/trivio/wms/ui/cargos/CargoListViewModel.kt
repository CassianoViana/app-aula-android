package br.com.trivio.wms.ui.cargos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.repository.CargosRepository
import kotlinx.coroutines.launch

class CargoListViewModel(
  private val cargosRepository: CargosRepository = CargosRepository()
) :
  ViewModel() {

  val cargosResult = MutableLiveData<Result<List<CargoListDto>>>()

  fun loadCargos() {
    viewModelScope.launch {
      cargosResult.value = asyncRequest { cargosRepository.loadMyPendingOrDoingCargos() }
    }
  }

  fun filter(filterString: String): List<CargoListDto> {
    return when (val value = cargosResult.value) {
      is Result.Success -> value.data.filter {
        it.search(filterString)
      }
      else -> listOf()
    }
  }
}
