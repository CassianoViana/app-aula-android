package br.com.trivio.wms.extensions

import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setPadding
import br.com.trivio.wms.R


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
    message.view.setPadding(15)
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
