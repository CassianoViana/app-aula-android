package br.com.trivio.wms.ui.cargos

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.repository.CargosRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CargoArrivalsViewModel(
  private val cargosRepository: CargosRepository = CargosRepository()
) :
  ViewModel() {

  val cargosResult = MutableLiveData<Result<List<CargoListDto>>>()

  fun loadTasks() {
    viewModelScope.launch {
      cargosResult.apply {
        value = withContext(Dispatchers.IO) {
          cargosRepository.loadCargoArrivals()
        }
      }
    }
  }
}
