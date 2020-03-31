package br.com.trivio.wms.api

import org.junit.Before
import org.junit.Test
import java.io.IOException

class ServerBackendTest {

  private val apiAddress = "http://192.168.15.7:8080"
  var retrofitConfig = ServerBackend()

  @Before
  fun config() {
    retrofitConfig.config(apiAddress)
  }

  @Test
  @Throws(IOException::class)
  fun login() {
    val usernamePassword = UsernamePassword("AMANCIO", "123")
    retrofitConfig.login(usernamePassword)
  }
}
