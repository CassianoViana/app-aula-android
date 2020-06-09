package br.com.trivio.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import br.com.trivio.wms.api.ServerBackend
import br.com.trivio.wms.data.GlobalData
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.extensions.endLoading
import br.com.trivio.wms.extensions.startLoading
import br.com.trivio.wms.ui.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.joda.time.LocalDateTime
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

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
  return withContext(Dispatchers.IO) {
    try {
      val userDetails = serverBackend.getUserDetails()
      globalData.userDetails = userDetails
      userDetails
    } catch (e: Exception) {
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
  val c = Calendar.getInstance();
  c[Calendar.YEAR] = this.year
  c[Calendar.DAY_OF_MONTH] = this.dayOfMonth
  c[Calendar.HOUR_OF_DAY] = this.hourOfDay
  c[Calendar.MINUTE] = this.minuteOfHour
  c[Calendar.SECOND] = this.secondOfMinute
  c[Calendar.MILLISECOND] = this.millisOfSecond
  return SimpleDateFormat(s).format(c.time)
}

fun View.setStripeColor(position: Int) {
  val white = context.getColor(R.color.colorWhite)
  val gray = context.getColor(R.color.lighterGray2)
  setBackgroundColor(if (position % 2 == 0) gray else white)
}

fun <T : Any> threatResult(
  result: Result<T>,
  always: ((result: Result<T>) -> Unit)? = null,
  onError: ((result: Result.Error) -> Unit)? = null,
  onSuccess: (result: Result.Success<T>) -> Unit
) {
  when (result) {
    is Result.Success -> {
      onSuccess(result)
    }
    is Result.Error -> {
      onError?.let {
        it(result)
      }
      showErrorMessage(result)
    }
  }
  always?.let { it(result) }
}

fun showErrorMessage(it: Result.Error) {
  val throwable = it.throwable
  val message = throwable.message
  message?.let {
    Toast.makeText(globalData.appContext, message, Toast.LENGTH_LONG).show()
  }
  throwable.printStackTrace()
}

object UiUtils {
  fun setTaskStatusStyle(textView: TextView, taskDto: TaskDto) {
    textView.text = taskDto.statusDto?.name
    textView.setTagBackground(taskDto.statusDto?.color)
  }
}

fun Fragment.startLoading() {
  (activity as AppCompatActivity).startLoading()
}

fun Fragment.endLoading() {
  (activity as AppCompatActivity).endLoading()
}

fun ViewGroup.inflateToViewHolder(resourceId: Int): View {
  return LayoutInflater.from(this.context).inflate(resourceId, this, false)
}

fun formatNumber(number: BigDecimal?): String {
  if (number == null) return "0"
  return NumberFormat.getInstance().format(number)
}

fun coalesce(value1: String?, alternative: Int): String {
  if (!value1.isNullOrBlank()) {
    return value1
  }
  return globalData.appContext.getString(alternative)
}

fun stringSimilarity(originalString: String, comparisonString: String): Int {
  //val algorithm = LongestCommonSubsequence()
  val distance = FuzzySearch.partialRatio(originalString, comparisonString)
  Log.i("DISTANCE", "$originalString, $comparisonString, $distance")
  return distance
}

fun matchFilter(originalString: String, comparisonString: String): Boolean {
  //return originalString.contains(comparisonString)
  val a = originalString.toUpperCase(Locale.getDefault()).replace(".", " ")
  val b = comparisonString.toUpperCase(Locale.getDefault())
  return stringSimilarity(a, b) >= 80 || a.contains(b)
}

fun setProgressGradient(v: View, percent: Int, colorStart: Int, colorEnd: Int) {
  val colors = mutableListOf<Int>()
  repeat((0..1000).count()) {
    colors.add(
      if (it < percent * 10) {
        colorStart
      } else {
        colorEnd
      }
    )
  }
  val linearGradient = GradientDrawable(
    GradientDrawable.Orientation.LEFT_RIGHT,
    colors.toIntArray()
  )
  v.background = linearGradient
}

fun getPercent(a: Int, b: Int): Int {
  return ((a.toFloat() / b) * 100).roundToInt()
}




