package br.com.trivio.wms.extensions

import android.content.Context
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.globalData

fun <T> MyAppCompatActivity.inflate(layoutResource: Int): T {
  return layoutInflater.inflate(layoutResource, null) as T
}

fun MyAppCompatActivity.showMessage(
  text: String,
  length: Int = Toast.LENGTH_LONG,
  colorResource: Int
) {
  val message = Toast.makeText(this, text, length)
  colorResource.let {
    message.view = inflate<ViewGroup>(R.layout.toast).apply {
      this.findViewById<TextView>(R.id.toast_message).apply {
        this.background.setTint(getColor(colorResource))
        this.text = text
      }
    }
  }
  message.show()
}


fun MyAppCompatActivity.showMessageSuccess(
  stringResource: Int
) {
  val text = getString(stringResource)
  showMessageSuccess(text)
}

fun MyAppCompatActivity.showMessageSuccess(
  text: String
) {
  showMessage(text, colorResource = R.color.success)
}

fun MyAppCompatActivity.showMessageError(
  resourceText: Int
) {
  val text = getString(resourceText)
  showMessageError(text)
}

fun MyAppCompatActivity.showMessageError(
  text: String
) {
  showMessage(text, colorResource = R.color.error)
}

fun MyAppCompatActivity.showMessageInfo(
  resource: Int
) {
  showMessage(getString(resource), colorResource = R.color.info)
}

fun getErrorOrNull(context: Context, value: Int?): String? =
  if (value == null) null else context.getString(value)

fun showErrorMessage(it: Result.Error) {
  val throwable = it.throwable
  val message = throwable.message
  message?.let {
    Toast.makeText(
      globalData.appContext,
      message,
      Toast.LENGTH_LONG
    ).show()
  }
  throwable.printStackTrace()
}
