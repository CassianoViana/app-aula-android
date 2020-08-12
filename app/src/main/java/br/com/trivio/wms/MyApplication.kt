package br.com.trivio.wms

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Bundle
import android.util.Log
import androidx.preference.PreferenceManager
import br.com.trivio.wms.api.ServerBackend
import br.com.trivio.wms.data.GlobalData
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.extensions.asyncRequest
import br.com.trivio.wms.extensions.showErrorMessage
import br.com.trivio.wms.ui.login.LoginActivity
import java.io.IOException

val globalData: GlobalData = GlobalData()
val serverBackend = ServerBackend()
val lifecycleCallback = LifecycleCallbacks()

class LifecycleCallbacks : Application.ActivityLifecycleCallbacks {
  private val activities = mutableSetOf<MyAppCompatActivity>()
  override fun onActivityPaused(activity: Activity?) {
    printLog("onActivityPaused", activity)
  }

  override fun onActivityResumed(activity: Activity?) {
    printLog("onActivityResumed", activity)
  }

  override fun onActivityStarted(activity: Activity?) {
    printLog("onActivityStarted", activity)
    activity?.let {
      if (activity is MyAppCompatActivity)
        activities.add(activity)
    }
  }

  override fun onActivityDestroyed(activity: Activity?) {
    printLog("onActivityDestroyed", activity)
    activity?.let {
      if (activity is MyAppCompatActivity)
        activities.remove(activity)
    }
  }

  override fun onActivitySaveInstanceState(activity: Activity?, p1: Bundle?) {
    printLog("onActivitySaveInstanceState", activity)
  }

  override fun onActivityStopped(activity: Activity?) {
    printLog("onActivityStopped", activity)
  }

  override fun onActivityCreated(activity: Activity?, p1: Bundle?) {
    printLog("onActivityCreated", activity)
  }

  fun closeAllActivities() {
    activities.forEach { it.finish() }
  }

  private fun printLog(methodName: String, activity: Activity?) {
    Log.i("lifecycle", activities.joinToString(",") { it.localClassName })
    Log.i("lifecycle", "$methodName: ${activity?.localClassName}")
  }

}

class MyApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    globalData.appContext = applicationContext
    loadApiSettingsFromPreferences(this)
    registerActivityLifecycleCallbacks(lifecycleCallback)
  }
}

fun loadApiSettingsFromPreferences(context: Context) {
  val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  val isDevMode = preferences.getBoolean("developer_mode", false)
  val addressPrefKey = if (isDevMode) "local_url" else "server_url"
  val address = preferences.getString(addressPrefKey, "https://api.wms.trivio.com.br")
  if (address != null) {
    serverBackend.config(address)
    serverBackend.onUnauthorized = {
      val loginActivity = LoginActivity::class.java
      try {
        lifecycleCallback.closeAllActivities()
      } finally {
        val intent = Intent(context, loginActivity)
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
      }
    }
  }
}

suspend fun loadUserDetails(): UserDetails {
  Log.i("LoadUserDetails", "loadUserDetails")
  var userDetails = UserDetails()
  try {
    userDetails = asyncRequest { serverBackend.getUserDetails() }
    globalData.userDetails = userDetails
  } catch (e: Exception) {
    e.printStackTrace()
  }
  return userDetails
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

fun <T : Any> onResult(
  result: Result<T>,
  always: ((result: Result<T>) -> Unit) = {},
  onError: ((result: Result.Error) -> Unit) = {},
  onNullResult: ((result: Result.Null<T?>) -> Unit) = {},
  onSuccess: (result: Result.Success<T>) -> Unit
) {
  when (result) {
    is Result.Success -> {
      onSuccess(result)
    }
    is Result.Error -> {
      onError(result)
      showErrorMessage(result)
    }
    is Result.Null -> {
      onNullResult(result)
    }
  }
  always(result)
}
