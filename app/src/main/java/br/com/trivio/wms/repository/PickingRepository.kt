package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.PickingItemDto
import br.com.trivio.wms.serverBackend
import java.math.BigDecimal

class PickingRepository {

  fun loadMyPendingOrDoingPickings() =
    Result.call { serverBackend.getMyPendingPickings() }


  fun loadPickingTask(taskId: Long) =
    Result.call { serverBackend.getPickingTask(taskId) }

  fun startPicking(taskId: Long) = Result.call { serverBackend.startPicking(taskId)}
  fun pickItem(item: PickingItemDto, quantity: BigDecimal) = Result.call { serverBackend.pickItem(item, quantity) }

}
