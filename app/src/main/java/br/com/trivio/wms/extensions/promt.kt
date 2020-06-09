package br.com.trivio.wms.extensions

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import br.com.trivio.wms.R

fun AppCompatActivity.prompt(
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
