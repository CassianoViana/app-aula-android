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
  val currentFilter = MutableLiveData<String>()

  fun loadCargos(status: String = "PENDING") {
    loadCargosAndPutInResult(status, cargosResult)
  }

  private fun loadCargosAndPutInResult(
    status: String,
    result: MutableLiveData<Result<List<CargoListDto>>>
  ) {
    currentFilter.value = status
    viewModelScope.launch {
      result.value = asyncRequest { cargosRepository.loadCargosByStatus(status) }
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

  fun toggleBetweenStartedOrPendingCargosConferences() {
    val status = when (currentFilter.value) {
      "DOING" -> "PENDING"
      "PENDING" -> "DOING"
      else -> "PENDING"
    }
    this.loadCargos(status)
  }
}
