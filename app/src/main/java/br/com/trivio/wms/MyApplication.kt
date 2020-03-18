package br.com.trivio.wms

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import br.com.trivio.wms.api.RetrofitConfig
import br.com.trivio.wms.data.GlobalData
import br.com.trivio.wms.data.model.TaskType
import br.com.trivio.wms.data.model.UserDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

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
  Log.i("LoadUserDetails", "loadUserDetails")
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

fun TextView.setTagBackground(color: String?) {
  color?.let {
    (background as GradientDrawable).setColor(Color.parseColor(it))
  }
}

@SuppressLint("SimpleDateFormat")
fun LocalDateTime.formatTo(s: String): CharSequence? {
  val out = Date.from(atZone(ZoneId.systemDefault()).toInstant())
  return SimpleDateFormat(s).format(out)
}

fun View.setStripeColor(position: Int) {
  val white = context.getColor(R.color.colorWhite)
  val gray = context.getColor(R.color.lighterGray)
  setBackgroundColor(if (position % 2 == 0) gray else white)
}
