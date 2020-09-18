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

class ServerBackend {
  private lateinit var api: Api
  lateinit var onUnauthorized: () -> Unit

  fun config(baseUrl: String) {

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
    api = retrofit.create(Api::class.java)
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

  private fun buildHttpClient(): OkHttpClient {
    val headerAuthorizationInterceptor = HeaderAuthorizationInterceptor()
    return OkHttpClient.Builder()
      .addInterceptor(headerAuthorizationInterceptor)
      .build()
  }

  @Throws(IOException::class)
  fun login(usernamePassword: UsernamePassword?): String? {
    val token: String?
    val login = api.login(usernamePassword)
    val execute: Response<ResponseBody> = login.execute()
    token = execute.body()?.string()
    return token
  }

  fun getUserDetails(): UserDetails = executeAndReturn(api.userDetails)

  fun getTasksByUser(userId: Long): List<TaskDto> = executeAndReturn(api.getTasksByUser(userId))

  fun getTask(id: Long): TaskDto = executeAndReturn(api.getTask(id))

  fun getCargoConferenceTask(id: Long): CargoConferenceDto =
    executeAndReturn(api.getCargoConference(id, true))

  fun getCargoConferenceTaskWithoutItems(taskId: Long): CargoConferenceDto {
    return executeAndReturn(api.getCargoConference(taskId, false))
  }

  fun startCargoConference(taskId: Long): TaskStatusDto {
    return executeAndReturn(api.startCargoConference(taskId))
  }

  fun finishCargoConference(taskId: Long): TaskStatusDto =
    executeAndReturn(api.finishCargoConference(taskId))

  fun restartCargoConference(taskId: Long): CargoConferenceDto =
    executeAndReturn(api.restartCargoConference(taskId))

  fun getCargosByStatus(status: String): List<CargoListDto> =
    executeAndReturn(api.getCargosByStatus(status))

  fun getMyPendingCargos(): MutableList<CargoListDto> =
    executeAndReturn(api.cargosPendingToOperatorCheck)

  fun countCargoItem(
    cargoConferenceItemDto: CargoConferenceItemDto,
    quantity: BigDecimal,
    description: String? = null
  ) = execute(api.countCargoItem(cargoConferenceItemDto.id, quantity, description))

  fun finishTask(taskId: Long) = execute(api.finishTask(taskId))

  fun loadCountsHistory(taskId: Long): List<ConferenceCountDto> =
    executeAndReturn(api.getCountsHistory(taskId))

  fun undoCountHistoryItem(conferenceCountHistoryItemId: Long?) =
    execute(api.undoCountHistoryItem(conferenceCountHistoryItemId))

  fun getMyPendingPickings(): List<PickingListDto> = FakeApi.getPickingsPendingToOperatorCheck()
  fun getPickingTask(taskId:Long): PickingTaskDto = FakeApi.getPickingTask()
  fun getEquipments(taskId:Long): List<EquipmentDto> = FakeApi.getEquipments()
  fun startPicking(taskId: Long) : TaskStatusDto = FakeApi.startPicking(taskId);
  fun pickItem(item: PickingItemDto, quantity: BigDecimal):PickingItemDto = FakeApi.pickItem(item, quantity)

}
