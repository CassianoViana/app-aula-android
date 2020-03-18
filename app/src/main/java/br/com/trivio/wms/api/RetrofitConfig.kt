package br.com.trivio.wms.api

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

class RetrofitConfig {
  private lateinit var api: Api

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

  private fun <T> getResultOrExceptionFrom(call: Call<T>): T {
    val response = call.execute()
    return if (response.isSuccessful) {
      response.body()!!
    } else {
      throw IOException(
        """
          API call error:
          Code: ${response.code()}
          Message: ${response.message()}
          ErrorBody: ${response.errorBody()?.string()}
        """
      )
    }
  }

  private fun buildHttpClient(): OkHttpClient {
    val headerAuthorizationInterceptor = HeaderAuthorizationInterceptor()
    return OkHttpClient.Builder()
      .addInterceptor(headerAuthorizationInterceptor) // This is used to add ApplicationInterceptor.
      .addNetworkInterceptor(headerAuthorizationInterceptor) //This is used to add NetworkInterceptor.
      .build()
  }

  @Throws(IOException::class)
  fun login(usernamePassword: UsernamePassword?): String? {
    var token: String? = ""
    val login = api.login(usernamePassword)
    val execute: Response<ResponseBody> = login.execute()
    token = execute.body()?.string()
    return token
  }

  fun getUserDetails(): UserDetails {
    return getResultOrExceptionFrom(api.userDetails)
  }

  fun getTasksByUser(userId: Long): List<TaskDto> {
    return getResultOrExceptionFrom(api.getTasksByUser(userId))
  }

  fun getTask(id: Long): TaskDto {
    return getResultOrExceptionFrom(api.getTask(id))
  }
}
