package br.com.trivio.wms.extensions

import android.app.Dialog
import android.text.InputType
import android.view.View
import android.widget.*
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.NumberInput
import kotlinx.android.synthetic.main.custom_number_input.view.*

fun MyAppCompatActivity.prompt(
  firstTitle: String,
  secondTitle: String,
  positiveAction: (dialog: Dialog, value: String) -> Unit,
  negativeButtonText: String = getString(R.string.cancel),
  negativeAction: ((dialog: Dialog) -> Unit)? = null,
  closeAction: ((dialog: Dialog) -> Unit)? = null,
  inputType: Int = InputType.TYPE_CLASS_NUMBER,
  inputValue: Any? = null,
  hint: String = "0,00",
  inputView: View? = null,
  viewsToAdd: List<View> = listOf()
): Dialog {
  val layout = inflate<View>(R.layout.custom_prompt)
  val confirmButton = layout.findViewById<Button>(R.id.positive_action)
  val negativeButton = layout.findViewById<Button>(R.id.negative_action)
  val editText = layout.findViewById<EditText>(R.id.input_value)
  val btnClose = layout.findViewById<ImageView>(R.id.btn_icon_x)
  val layoutAddViews = layout.findViewById<LinearLayout>(R.id.more_views_to_add)

  if (inputView != null) {
    editText.setVisible(false)
    val parent = editText.parent as LinearLayout
    val indexOfDefaultInput = parent.indexOfChild(editText)
    parent.addView(inputView, indexOfDefaultInput)
  }

  editText.inputType = inputType
  editText.hint = hint
  inputValue?.let {
    editText.setText(it.toString())
  }

  val firstTextView = layout.findViewById<TextView>(R.id.first_title)
  val secondTextView = layout.findViewById<TextView>(R.id.second_title)
  secondTextView.text = secondTitle
  firstTextView.text = firstTitle

  val dialog = Dialog(this, R.style.full_screen_dialog)
  dialog.setContentView(layout)
  dialog.show()

  confirmButton.setOnClickListener {
    positiveAction(dialog, editText.text.toString())
    dialog.hide()
  }

  negativeButton.setVisible(negativeAction != null)
  negativeButton.text = negativeButtonText

  negativeButton.setOnClickListener {
    negativeAction.let {
      it?.invoke(dialog)
    }
    dialog.hide()
  }

  btnClose.setOnClickListener {
    closeAction?.let { it.invoke(dialog) }
    dialog.hide()
  }

  viewsToAdd.forEach { layoutAddViews.addView(it) }

  var editToShowKeyboard: EditText = editText
  if (inputView != null) {
    if (inputView is NumberInput) {
      editToShowKeyboard = inputView.custom_number_input
    }
  }
  showKeyboard(editToShowKeyboard)

  return dialog
}

