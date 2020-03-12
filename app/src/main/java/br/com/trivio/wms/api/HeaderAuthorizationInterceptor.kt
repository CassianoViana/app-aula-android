package br.com.trivio.wms.api

import br.com.trivio.wms.data.globalData
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

class HeaderAuthorizationInterceptor : Interceptor {
  override fun intercept(chain: Interceptor.Chain): Response {
    val response: Response
    val request: Request = chain.request()
    val token = globalData.token
    val newRequest = request.newBuilder().apply {
      addHeader("Accept", "application/json")
      addHeader("Accept", "text/plain")
      if (!token.isNullOrBlank()) {
        addHeader("Authorization", "Bearer $token")
      }
    }.build()
    response = chain.proceed(newRequest)
    return response
  }

}
