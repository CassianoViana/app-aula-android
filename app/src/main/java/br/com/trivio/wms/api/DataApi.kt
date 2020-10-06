package br.com.trivio.wms.api

import br.com.trivio.wms.data.dto.*
import br.com.trivio.wms.data.model.UserDetails
import java.math.BigDecimal

interface DataApi {

  fun getMyPendingPickings(): List<PickingListDto>
  fun login(usernamePassword: UsernamePassword): String

  fun getUserDetails(): UserDetails
  fun getTasksByUser(userId: Long): List<TaskDto>
  fun getTask(id: Long): TaskDto
  fun getCargoConferenceTask(id: Long, fetchItens:Boolean): CargoConferenceDto
  fun getCargoConferenceTaskWithoutItems(taskId: Long): CargoConferenceDto
  fun startCargoConference(taskId: Long): StatusDto
  fun finishCargoConference(taskId: Long): StatusDto
  fun restartCargoConference(taskId: Long): CargoConferenceDto
  fun getCargosByStatus(status: String): List<CargoListDto>
  fun getMyPendingCargos(): MutableList<CargoListDto>
  fun countCargoItem(
    cargoConferenceItemDto: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String?
  )

  fun finishTask(taskId: Long)
  fun loadCountsHistory(taskId: Long): List<ConferenceCountDto>
  fun undoCountHistoryItem(conferenceCountHistoryItemId: Long?)
  fun getPickingTask(taskId: Long): PickingTaskDto
  fun getEquipments(taskId: Long): List<EquipmentDto>
  fun setSelectedEquipments(taskId: Long, equipmentsIds: List<Long>): List<Long>
  fun removeEquipment(id: Long): Any
}
