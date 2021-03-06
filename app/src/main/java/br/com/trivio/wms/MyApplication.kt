package br.com.trivio.wms

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import br.com.trivio.wms.api.ServerBackend
import br.com.trivio.wms.data.GlobalData
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.ui.login.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.io.IOException
import java.math.BigDecimal
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
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
  val out = Date.from(atZone(ZoneId.systemDefault()).toInstant())
  return SimpleDateFormat(s).format(out)
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

fun AppCompatActivity.handleHomeClickFinish(
  item: MenuItem?,
  function: (() -> Unit)? = null
) {
  when (item?.itemId) {
    android.R.id.home -> {
      function?.let {
        it()
      }
      finish()
    }
  }
}

fun AppCompatActivity.setupToolbar(resourceStringTitle: Int = 0, showHomeButton: Boolean = true) {
  val toolbar = this.findViewById<Toolbar>(R.id.toolbar)
  if (resourceStringTitle != 0)
    toolbar.title = this.getString(resourceStringTitle)
  this.setSupportActionBar(toolbar)
  this.supportActionBar?.setDisplayHomeAsUpEnabled(showHomeButton)
}

object UiUtils {
  fun setTaskStatusStyle(textView: TextView, taskDto: TaskDto) {
    textView.text = taskDto.statusDto?.name
    textView.setTagBackground(taskDto.statusDto?.color)
  }
}

@SuppressLint("WrongViewCast")
fun AppCompatActivity.getProgressBarLayout(): FrameLayout? {
  return findViewById(R.id.layout_progress_bar)
}

fun AppCompatActivity.startLoading(loadingStringId: Int? = null) {
  getProgressBarLayout()?.let { layout ->
    layout.visibility = View.VISIBLE
    loadingStringId?.let {
      layout.findViewById<TextView>(R.id.loading_text).text = getString(it)
    }
  }
}

fun AppCompatActivity.endLoading() {
  getProgressBarLayout()?.visibility = View.GONE
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

fun AppCompatActivity.hideKeyboard(editText: EditText) {
  val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.hideSoftInputFromWindow(editText.windowToken, 0)
}

fun AppCompatActivity.showKeyboard(editText: EditText? = null) {
  val imm: InputMethodManager =
    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
  imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
  editText?.requestFocus()
  editText?.selectAll()
}

fun <T> AppCompatActivity.inflate(layoutResource: Int): T {
  return layoutInflater.inflate(layoutResource, null) as T
}

fun AppCompatActivity.showMessage(
  text: String,
  length: Int = Toast.LENGTH_LONG,
  colorResource: Int
) {
  val message = Toast.makeText(this, text, length)
  colorResource.let {
    message.view.findViewById<TextView>(android.R.id.message)
      .setTextColor(getColor(R.color.colorWhite))
    message.view.setBackgroundColor(getColor(colorResource))
  }
  message.show()
}


fun AppCompatActivity.showMessageSuccess(
  stringResource: Int
) {
  val text = getString(stringResource)
  showMessageSuccess(text)
}

fun AppCompatActivity.showMessageSuccess(
  text: String
) {
  showMessage(text, colorResource = R.color.success)
}

fun AppCompatActivity.showMessageError(
  resourceText: Int
) {
  val text = getString(resourceText)
  showMessageError(text)
}

fun AppCompatActivity.showMessageError(
  text: String
) {
  showMessage(text, colorResource = R.color.error)
}

fun AppCompatActivity.showMessageInfo(
  resource: Int
) {
  showMessage(getString(resource), colorResource = R.color.info)
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

fun AppCompatActivity.showFinishTaskDialog(
  message: Int,
  listener: DialogInterface.OnClickListener
) {
  AlertDialog.Builder(this)
    .setCancelable(false)
    .setTitle(R.string.finish_task_label)
    .setMessage(message)
    .setPositiveButton(R.string.finish_task_label, listener)
    .setNegativeButton(R.string.cancel, null)
    .show()
}

fun AppCompatActivity.startRequestValue(
  firstTitle: String,
  secondTitle: String,
  closeAction: () -> Unit = {},
  viewsToAdd: List<View> = listOf(),
  keepClosedOnCreate: Boolean = false,
  inputType: Int = InputType.TYPE_CLASS_NUMBER,
  inputValue: Any? = null,
  hint: String = "0,00",
  positiveAction: (dialog: Dialog, value: String) -> Unit
): Dialog {
  val layout = inflate<View>(R.layout.activity_input_number)
  val confirmButton = layout.findViewById<Button>(R.id.confirm_button)
  val inputValueEditText = layout.findViewById<EditText>(R.id.input_value)
  val btnClose = layout.findViewById<Button>(R.id.close_btn)
  val layoutAddViews = layout.findViewById<LinearLayout>(R.id.more_views_to_add)

  inputValueEditText.inputType = inputType
  inputValueEditText.hint = hint
  inputValue?.let {
    inputValueEditText.setText(it.toString())
  }

  val firstTextView = layout.findViewById<TextView>(R.id.first_title)
  val secondTextView = layout.findViewById<TextView>(R.id.second_title)
  secondTextView.text = secondTitle
  firstTextView.text = firstTitle

  val dialog = Dialog(this, R.style.full_screen_dialog)
  dialog.setContentView(layout)

  if (!keepClosedOnCreate) {
    dialog.show()
  }

  confirmButton.setOnClickListener {
    positiveAction(dialog, inputValueEditText.text.toString())
  }

  btnClose.setOnClickListener {
    closeAction()
    dialog.hide()
  }

  viewsToAdd.forEach { layoutAddViews.addView(it) }

  showKeyboard(inputValueEditText)

  return dialog
}

fun AppCompatActivity.createButton(name: String, onClick: (() -> Unit) = {}): Button {
  val button = inflate<Button>(R.layout.button)
  button.text = name
  button.setOnClickListener {
    onClick()
  }
  return button
}
