package br.com.trivio.wms.api

import br.com.trivio.wms.data.dto.*
import br.com.trivio.wms.data.model.UserDetails
import java.io.IOException
import java.math.BigDecimal

class ServerBackend {
  private lateinit var dataApi: DataApi

  fun config(baseUrl: String, useFakeApi: Boolean = false, onUnauthorized: () -> Unit) {
    dataApi = if (useFakeApi) {
      FakeApi
    } else {
      RealApi.apply {
        config(baseUrl, onUnauthorized)
      }
    }
  }

  @Throws(IOException::class)
  fun login(usernamePassword: UsernamePassword): String? =
    dataApi.login(usernamePassword)

  fun getUserDetails(): UserDetails =
    dataApi.getUserDetails()

  fun getTasksByUser(userId: Long): List<TaskDto> =
    dataApi.getTasksByUser(userId)

  fun getTask(id: Long): TaskDto =
    dataApi.getTask(id)

  fun getCargoConferenceTask(id: Long): CargoConferenceDto =
    dataApi.getCargoConferenceTask(id, true)

  fun getCargoConferenceTaskWithoutItems(taskId: Long) =
    dataApi.getCargoConferenceTask(taskId, false)

  fun startCargoConference(taskId: Long): StatusDto =
    dataApi.startCargoConference(taskId)

  fun finishCargoConference(taskId: Long): StatusDto =
    dataApi.finishCargoConference(taskId)

  fun restartCargoConference(taskId: Long): CargoConferenceDto =
    dataApi.restartCargoConference(taskId)

  fun getCargosByStatus(status: String): List<CargoListDto> =
    dataApi.getCargosByStatus(status)

  fun getMyPendingCargos(): MutableList<CargoListDto> =
    dataApi.getMyPendingCargos()

  fun countCargoItem(
    cargoConferenceItemDto: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String? = null
  ) = dataApi.countCargoItem(cargoConferenceItemDto, quantity, description)

  fun finishTask(taskId: Long) = dataApi.finishTask(taskId)

  fun loadCountsHistory(taskId: Long): List<ConferenceCountDto> =
    dataApi.loadCountsHistory(taskId)

  fun undoCountHistoryItem(conferenceCountHistoryItemId: Long?) =
    dataApi.undoCountHistoryItem(conferenceCountHistoryItemId)

  fun getMyPendingPickings(): List<PickingListDto> =
    dataApi.getMyPendingPickings()

  fun getPickingTask(taskId: Long): PickingTaskDto = FakeApi.getPickingTask()
  fun getEquipments(taskId: Long): List<EquipmentDto> = FakeApi.getEquipments()
  fun startPicking(taskId: Long): StatusDto = FakeApi.startPicking(taskId);
  fun pickItem(item: PickingItemDto, quantity: BigDecimal): PickingItemDto =
    FakeApi.pickItem(item, quantity)

  fun addEquipments(ids: LongArray, taskId: Long): LongArray = FakeApi.addEquipments(ids)
  fun removeEquipment(id: Long, taskId: Long): EquipmentDto = FakeApi.removeEquipment(id)

}
