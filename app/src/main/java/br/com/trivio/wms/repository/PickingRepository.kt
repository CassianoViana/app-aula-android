package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.PickingItemDto
import br.com.trivio.wms.data.dto.StatusDto
import br.com.trivio.wms.serverBackend
import java.math.BigDecimal

class PickingRepository {

  fun loadMyPendingOrDoingPickings() =
    Result.call { serverBackend.getMyPendingPickings() }


  fun loadPickingTask(taskId: Long) =
    Result.call { serverBackend.getPickingTask(taskId) }

  fun startPicking(taskId: Long) = Result.call { serverBackend.startPicking(taskId) }
  fun pickItem(item: PickingItemDto, position: String, quantity: BigDecimal) =
    Result.call { serverBackend.pickItem(item, position, quantity) }

  fun finishPicking(taskId: Long): Result<StatusDto> =
    Result.call { serverBackend.finishPickingTask(taskId) }

  fun cancelPickingRepositionRequest(item: PickingItemDto): Result<PickingItemDto> =
    Result.call { serverBackend.cancelPickingRepositionRequest(item) }

  fun requestPickingReposition(item: PickingItemDto): Result<PickingItemDto> =
    Result.call { serverBackend.requestPickingReposition(item) }

  fun informItemNotFound(item: PickingItemDto): Result<PickingItemDto> =
    Result.call { serverBackend.informItemNotFound(item) }


}
