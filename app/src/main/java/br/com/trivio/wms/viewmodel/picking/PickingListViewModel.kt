package br.com.trivio.wms.viewmodel.picking

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.PickingListDto
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.repository.PickingRepository
import kotlinx.coroutines.launch

class PickingListViewModel(
  private val pickingsRepository: PickingRepository = PickingRepository()
) :
  ViewModel() {

  val pickingsResult = MutableLiveData<Result<List<PickingListDto>>>()

  fun loadPickings() {
    viewModelScope.launch {
      pickingsResult.value = asyncRequest { pickingsRepository.loadMyPendingOrDoingPickings() }
    }
  }

  fun filter(filterString: String): List<PickingListDto> {
    return when (val value = pickingsResult.value) {
      is Result.Success -> value.data.filter {
        it.search(filterString)
      }
      else -> listOf()
    }
  }
}
