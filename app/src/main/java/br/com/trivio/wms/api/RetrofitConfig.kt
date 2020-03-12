package br.com.trivio.wms.api

import br.com.trivio.wms.data.model.UserDetails
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.io.IOException
import java.lang.IllegalStateException

val api = RetrofitConfig()

class RetrofitConfig {
  private lateinit var api: WmsApi

  fun config(baseUrl: String) {
    val retrofit = Retrofit.Builder()
      .addConverterFactory(JacksonConverterFactory.create())
      .baseUrl(baseUrl)
      .client(buildHttpClient())
      .build()
    api = retrofit.create(WmsApi::class.java)
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
    return try {
      val userDetailsCall: Call<UserDetails> = api.userDetails
      val execute = userDetailsCall.execute()
      if (execute.isSuccessful) {
        execute.body()!!
      } else {
        throw IOException(execute.errorBody()?.string())
      }
    } catch (t: Throwable) {
      t.printStackTrace()
      throw t
    }
  }
}
