package br.com.trivio.wms.api

import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.dto.DamageDto
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.UserDetails
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException
import java.lang.IllegalStateException

class ServerBackend {
  private lateinit var api: Api
  lateinit var onUnauthorized: () -> Unit

  fun config(baseUrl: String) {

    val module = SimpleModule()
    //myModule.addDeserializer(TaskStatus::class.java, TaskStatusDeserializer())

    val mapper = ObjectMapper()
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .registerModule(JavaTimeModule())
      .registerModule(module)

    val retrofit = Retrofit.Builder()
      .addConverterFactory(JacksonConverterFactory.create(mapper))
      .baseUrl(baseUrl)
      .client(buildHttpClient())
      .build()
    api = retrofit.create(Api::class.java)
  }

  private fun <T> executeAndReturn(call: Call<T>): T {
    val response = call.execute()
    if (response.isSuccessful) {
      val result = response.body()
      if (result != null) {
        return result
      } else {
        throw IllegalStateException("Não foi possível buscar os dados")
      }
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

  fun getUserDetails(): UserDetails {
    return executeAndReturn(api.userDetails)
  }

  fun getTasksByUser(userId: Long): List<TaskDto> {
    return executeAndReturn(api.getTasksByUser(userId))
  }

  fun getTask(id: Long): TaskDto {
    return executeAndReturn(api.getTask(id))
  }

  fun getCargoConference(id: Long): CargoConferenceDto {
    return executeAndReturn(api.getCargoConference(id))
  }

  fun countCargoItem(cargoConferenceItemDto: CargoConferenceItemDto) {
    return execute(api.countCargoItem(cargoConferenceItemDto))
  }

  fun registerDamage(damageDto: DamageDto) {
    return execute(api.registerDamage(damageDto))
  }

  fun finishTask(taskId: Long) {
    return execute(api.finishTask(taskId))
  }
}
