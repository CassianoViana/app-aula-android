package br.com.trivio.wms

import android.app.Application
import android.content.Context
import androidx.preference.PreferenceManager
import br.com.trivio.wms.api.RetrofitConfig
import br.com.trivio.wms.data.GlobalData
import br.com.trivio.wms.data.model.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.lang.IllegalStateException

val globalData: GlobalData = GlobalData()
val api = RetrofitConfig()

class MyApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    globalData.appContext = applicationContext
    loadApiSettingsFromPreferences(this)
  }
}

fun loadApiSettingsFromPreferences(context: Context) {
  val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  val address = preferences.getString("server_url", null)
  if (address != null)
    api.config(address)
}

suspend fun loadUserDetails(): UserDetails {
  return withContext(Dispatchers.IO) {
    try {
      val userDetails = api.getUserDetails()
      globalData.userDetails = userDetails
      userDetails
    } catch (e: Exception) {
      //Toast.makeText(this@MainActivity, e.localizedMessage.toString(), Toast.LENGTH_LONG).show()
      e.printStackTrace()
      UserDetails()
    }
  }
}

fun getErrorMessageCode(context: String, throwable: Throwable): Int {
  return when (context) {
    "login" -> when (throwable) {
      is IOException -> R.string.connection_failed
      is IllegalStateException -> R.string.could_not_retrieve_token
      else -> R.string.login_failed
    }
    else -> R.string.default_error_message
  }
}
