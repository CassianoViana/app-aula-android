package br.com.trivio.wms.api

import br.com.trivio.wms.data.dto.*
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.data.model.UserDetails
import org.joda.time.LocalDateTime
import java.math.BigDecimal

object FakeApi : DataApi {

  fun getEquipments(qtd: Int = 30): List<EquipmentDto> {
    return mutableListOf<EquipmentDto>().apply {
      for (i in 0 until qtd) {
        this.add(
          EquipmentDto(
            id = i.toLong(),
            name = "Equipment $i",
            code = "${i}000",
            selected = false
          )
        )
      }
    }
  }

  override fun getMyPendingPickings(): List<PickingListDto> {
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

  override fun login(usernamePassword: UsernamePassword): String {
    return "fake-token"
  }

  override fun getUserDetails(): UserDetails {
    return UserDetails(
      id = 20,
      ownerId = 1,
      name = "Jon Doe",
      username = "Doe",
      roleDescription = "Developer",
      "DEV"
    )
  }

  override fun getTasksByUser(userId: Long): List<TaskDto> {
    return listOf()
  }

  override fun getTask(id: Long): TaskDto {
    return TaskDto()
  }

  override fun getCargoConferenceTask(id: Long, fetchItens: Boolean): CargoConferenceDto {
    return CargoConferenceDto()
  }

  override fun getCargoConferenceTaskWithoutItems(taskId: Long): CargoConferenceDto {
    return CargoConferenceDto()
  }

  override fun startCargoConference(taskId: Long): StatusDto {
    return StatusDto()
  }

  override fun finishCargoConference(taskId: Long): StatusDto {
    return StatusDto()
  }

  override fun restartCargoConference(taskId: Long): CargoConferenceDto {
    return CargoConferenceDto()
  }

  override fun getCargosByStatus(status: String): List<CargoListDto> {
    return listOf()
  }

  override fun getMyPendingCargos(): MutableList<CargoListDto> {
    return mutableListOf()
  }

  override fun countCargoItem(
    cargoConferenceItemDto: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String?
  ) {

  }

  override fun finishTask(taskId: Long) {

  }

  override fun loadCountsHistory(taskId: Long): List<ConferenceCountDto> {
    return listOf()
  }

  override fun undoCountHistoryItem(conferenceCountHistoryItemId: Long?) {

  }

  fun getPickingTask(): PickingTaskDto {
    val statusList = arrayOf(
      StatusDto(name = "Atendido", color = "#339900"),
      StatusDto(name = "Atendido parcial", color = "#ffcc00"),
      StatusDto(name = "Não atendido", color = "#ff0000"),
      StatusDto(name = "Pendente", color = "#9e9e9e"),
    )
    return PickingTaskDto(
      id = 10,
      orderNumber = 1233,
      customerName = "Aline wolf e cia ltda",
      cargoNumber = "123",
      orderDate = LocalDateTime.now(),
      taskStatus = TaskStatus.PENDING,
      priorityStatus = StatusDto(name = "Pedido prioritario", color = "#ff0000"),
      quantityItems = 100,
      sellerCode = "3023",
      sellerName = "Anibal",
      quantityItemsToPick = 100,
      quantityPartiallyPicked = if (Math.random() * 10 > 5) 15 else 0,
      quantityPickedItems = 23,
      progress = 90,
      items = mutableListOf<PickingItemDto>().apply {
        for (i in 0 until 50) {
          this.add(
            PickingItemDto(
              id = i.toLong(),
              sku = "7891360491293",
              gtin = "123123123123$i",
              name = "oxford",
              position = "132.141.234.12.3$i",
              order = i + 1,
              totalItemsTask = 10,
              storageUnit = StorageUnitDto(id = 1, code = "UN"),
              expectedQuantityToPick = BigDecimal((Math.random() * 20).toInt()),
              pickedQuantity = BigDecimal((Math.random() * 20).toInt()),
              status = statusList[(Math.random() * statusList.size).toInt()],
              hasRequestedPickingReposition = Math.random() > 0.5,
              stockPositions = mutableListOf<PickStockPositionDto>().apply {
                for (i in 0 until 8) {
                  add(
                    PickStockPositionDto(
                      name = "00${i}.00${i + 2}.00${i + 4}",
                      type = "Aéreo",
                      unity = "UN",
                      qtdItems = BigDecimal(10),
                    )
                  )
                }
              },
            )
          )
        }
      },
      equipments = getEquipments(5)
    )
  }

  fun startPicking(taskId: Long): StatusDto {
    return StatusDto(name = "DOING", color = "#ff0000")
  }

  fun pickItem(item: PickingItemDto, quantity: BigDecimal): PickingItemDto {
    return PickingItemDto(
      id = 0,
      sku = "123",
      gtin = "123123123123",
      name = "oxford",
      position = "123.123.123.123",
      storageUnit = StorageUnitDto(id = 1, referenceCode = "UD"),
      status = StatusDto("Atendido", "#339900"),
      hasRequestedPickingReposition = true,
    )
  }

  fun addEquipments(ids: LongArray): LongArray {
    return ids
  }

  fun removeEquipment(id: Long): EquipmentDto {
    return EquipmentDto(
      id = id,
      name = "Equipment $id",
      code = "${id}000",
      selected = false
    )
  }


}
