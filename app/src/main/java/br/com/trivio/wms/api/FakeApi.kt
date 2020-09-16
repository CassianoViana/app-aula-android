package br.com.trivio.wms.api

import br.com.trivio.wms.data.dto.PickingListDto
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.data.dto.StatusDto
import br.com.trivio.wms.data.model.TaskStatus
import org.joda.time.LocalDateTime

object FakeApi {
  fun getPickingsPendingToOperatorCheck(): List<PickingListDto> {
    return mutableListOf<PickingListDto>().apply {
      for (i in 0 until 30) {
        this.add(
          PickingListDto(
            id = 10,
            orderNumber = (Math.random() * 1000).toLong(),
            customerName = "Aline wolf e cia ltda",
            cargoNumber = "123",
            orderDate = LocalDateTime.now(),
            status = StatusDto(name = "Em andamento", color = "#ff0000"),
            priorityStatus = StatusDto(name = "Pedido prioritario", color = "#ff0000"),
            quantityItems = 100,
            progress = (Math.random() * 100).toInt()
          )
        )
      }
    }
  }

  fun getPickingTask(): PickingTaskDto {
    return PickingTaskDto(
      id = 10,
      orderNumber = 1233,
      customerName = "Aline wolf e cia ltda",
      cargoNumber = "123",
      orderDate = LocalDateTime.now(),
      status = TaskStatus.PENDING,
      priorityStatus = StatusDto(name = "Pedido prioritario", color = "#ff0000"),
      quantityItems = 100,
      sellerCode = "3023",
      sellerName = "Anibal",
      quantityItemsToPick = 100,
      quantityPickedItems = 23,
    )
  }
}
