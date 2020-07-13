package br.com.trivio.wms.ui.cargos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.extensions.callAsync
import br.com.trivio.wms.repository.CargosRepository
import kotlinx.coroutines.launch

class CargoListViewModel(
  private val cargosRepository: CargosRepository = CargosRepository()
) :
  ViewModel() {

  val cargosResult = MutableLiveData<Result<List<CargoListDto>>>()

  fun loadPendingCargos() {
    viewModelScope.launch {
      cargosResult.value = callAsync { cargosRepository.loadPendingCargos() }
    }
  }
}
