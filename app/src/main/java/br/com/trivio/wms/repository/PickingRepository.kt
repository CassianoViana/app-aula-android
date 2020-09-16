package br.com.trivio.wms.repository

import br.com.trivio.wms.data.Result
import br.com.trivio.wms.serverBackend

class PickingRepository {

  fun loadMyPendingOrDoingPickings() =
    Result.call { serverBackend.getMyPendingPickings() }


  fun loadPickingTask(taskId: Long) =
    Result.call { serverBackend.getPickingTask(taskId) }

}
