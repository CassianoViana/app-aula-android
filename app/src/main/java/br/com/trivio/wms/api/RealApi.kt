package br.com.trivio.wms.api

import br.com.trivio.wms.api.json.deserializer.MyLocalDateTimeDeserializer
import br.com.trivio.wms.data.dto.*
import br.com.trivio.wms.data.model.UserDetails
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.joda.time.LocalDateTime
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException
import java.math.BigDecimal

object RealApi : DataApi {

  internal lateinit var api: Api
  lateinit var onUnauthorized: () -> Unit

  fun config(baseUrl: String, onUnauthorized: () -> Unit) {
    val module = SimpleModule()
    //myModule.addDeserializer(TaskStatus::class.java, TaskStatusDeserializer())
    module.addDeserializer(LocalDateTime::class.java, MyLocalDateTimeDeserializer())

    val mapper = ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      //.registerModule(JavaTimeModule()) NOT compatible with marshmallow
      //.registerModule(JodaModule()) // compatible with marshmallow
      .registerModule(module)

    val retrofit = Retrofit.Builder()
      .addConverterFactory(JacksonConverterFactory.create(mapper))
      .baseUrl(baseUrl)
      .client(buildHttpClient())
      .build()

    this.onUnauthorized = onUnauthorized
    api = retrofit.create(Api::class.java)
  }

  private fun buildHttpClient(): OkHttpClient {
    val headerAuthorizationInterceptor = HeaderAuthorizationInterceptor()
    return OkHttpClient.Builder()
      .addInterceptor(headerAuthorizationInterceptor)
      .build()
  }

  override fun login(usernamePassword: UsernamePassword): String {
    val login = api.login(usernamePassword)
    val execute: Response<ResponseBody> = login.execute()
    val token = execute.body()?.string()
    return token!!
  }

  private fun <T> executeAndReturn(call: Call<T>): T {
    val result = executeAndReturnNullable(call)
    if (result != null) {
      return result
    } else {
      throw IllegalStateException("Não foi possível buscar os dados")
    }
  }

  private fun <T> executeAndReturnNullable(call: Call<T>): T? {
    val response = call.execute()
    if (response.isSuccessful) {
      return response.body()
    } else {
      throw handleUnauthorizedAndBuildFormattedError<T>(response)
    }
  }

  private fun <T> execute(call: Call<T>) {
    val response = call.execute()
    if (!response.isSuccessful) {
      throw handleUnauthorizedAndBuildFormattedError<T>(response)
    }
  }

  private fun <T> handleUnauthorizedAndBuildFormattedError(response: Response<T>): Throwable {
    try {
      return buildApiFormattedError(response)
    } finally {
      if (response.code() == 401) {
        onUnauthorized()
      }
    }
  }

  private fun <T> buildApiFormattedError(response: Response<T>): IOException {
    return IOException(
      """
        API call error:
        Code: ${response.code()}
        Message: ${response.message()}
        ErrorBody: ${response.errorBody()?.string()}
      """
    )
  }

  override fun getEquipments(taskId: Long): List<EquipmentDto> {
    return executeAndReturn(api.getEquipmentsFreeAndSelectedToTask(taskId))
  }

  override fun setSelectedEquipments(taskId: Long, equipmentsIds: List<Long>): List<Long> =
    executeAndReturn(api.setSelectedEquipments(taskId, equipmentsIds))

  override fun removeEquipment(id: Long) =
    execute(api.removeEquipment(id))

  override fun startPickingTask(taskId: Long): StatusDto =
    executeAndReturn(api.startPickingTask(taskId))

  override fun pickItem(
    item: PickingItemDto,
    quantity: BigDecimal,
    position: String
  ): PickingItemDto =
    executeAndReturn(api.pickItem(item.id, position, quantity))

  override fun finishPickingTask(taskId: Long): StatusDto =
    executeAndReturn(api.finishPickingTask(taskId))

  override fun cancelPickingRepositionRequest(item: PickingItemDto): PickingItemDto
    = executeAndReturn(api.cancelPickingRepositionRequest(item.id))

  override fun requestPickingReposition(item: PickingItemDto): PickingItemDto
  = executeAndReturn(api.requestPickingReposition(item.id))

  override fun informItemNotFound(item: PickingItemDto): PickingItemDto
    = executeAndReturn(api.informItemNotFound(item.id))

  override fun getUserDetails(): UserDetails = executeAndReturn(api.userDetails)

  override fun getTasksByUser(userId: Long): List<TaskDto> =
    executeAndReturn(api.getTasksByUser(userId))

  override fun getTask(id: Long): TaskDto = executeAndReturn(api.getTask(id))

  override fun getCargoConferenceTask(id: Long, fetchItems: Boolean): CargoConferenceDto =
    executeAndReturn(api.getCargoConference(id, fetchItems))

  override fun getCargoConferenceTaskWithoutItems(taskId: Long): CargoConferenceDto {
    return executeAndReturn(api.getCargoConference(taskId, false))
  }

  override fun startCargoConference(taskId: Long): StatusDto {
    return executeAndReturn(api.startCargoConference(taskId))
  }

  override fun finishCargoConference(taskId: Long): StatusDto =
    executeAndReturn(api.finishCargoConference(taskId))

  override fun restartCargoConference(taskId: Long): CargoConferenceDto =
    executeAndReturn(api.restartCargoConference(taskId))

  override fun getCargosByStatus(status: String): List<CargoListDto> =
    executeAndReturn(api.getCargosByStatus(status))

  override fun getMyPendingCargos(): MutableList<CargoListDto> =
    executeAndReturn(api.cargosPendingToOperatorCheck)

  override fun countCargoItem(
    cargoConferenceItemDto: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String?
  ) = execute(api.countCargoItem(cargoConferenceItemDto.id, quantity, description))

  override fun finishTask(taskId: Long) = execute(api.finishTask(taskId))

  override fun loadCountsHistory(taskId: Long): List<ConferenceCountDto> =
    executeAndReturn(api.getCountsHistory(taskId))

  override fun undoCountHistoryItem(conferenceCountHistoryItemId: Long?) =
    execute(api.undoCountHistoryItem(conferenceCountHistoryItemId))

  override fun getPickingTask(taskId: Long): PickingTaskDto =
    executeAndReturn(api.getPickingTask(taskId))

  override fun getMyPendingPickings(): MutableList<PickingListDto> =
    executeAndReturn(api.pickingsPendingToOperatorPick)
}
