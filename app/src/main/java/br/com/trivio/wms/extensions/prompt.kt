package br.com.trivio.wms.extensions

import android.app.Dialog
import android.text.InputType
import android.view.View
import android.view.ViewGroup
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
  positiveButtonText: String = getString(R.string.confirm),
  negativeAction: ((dialog: Dialog) -> Unit)? = null,
  closeAction: ((dialog: Dialog) -> Unit)? = null,
  inputType: Int = InputType.TYPE_CLASS_NUMBER,
  inputValue: Any? = null,
  hint: String = "0,00",
  inputView: View? = null,
  viewsBeforeInput: List<View> = listOf(),
  viewsAfterInput: List<View> = listOf(),
  viewsBeforeInputFn: (dialog: Dialog, views: MutableList<View>) -> Unit = { _, _ -> },
  viewsAfterInputFn: (dialog: Dialog, views: MutableList<View>) -> Unit = { _, _ -> },
  drawableConfirmButtonResId: Int? = null,
  dialog: Dialog? = null
): (Dialog) -> View {

  val createLayoutFn = { dialog: Dialog ->

    val layout = inflate<View>(R.layout.custom_prompt)
    val confirmButton = layout.findViewById<Button>(R.id.positive_action)
    val negativeButton = layout.findViewById<Button>(R.id.negative_action)
    val editText = layout.findViewById<EditText>(R.id.input_value)
    val btnClose = layout.findViewById<ImageView>(R.id.btn_icon_x)
    val layoutBeforeInput = layout.findViewById<LinearLayout>(R.id.views_to_add_before_input)
    val layoutAfterInput = layout.findViewById<LinearLayout>(R.id.views_to_add_after_input)

    if (inputView != null) {
      editText.setVisible(false)
      val parent = editText.parent as LinearLayout
      val indexOfDefaultInput = parent.indexOfChild(editText)
      inputView.parent?.let {
        if (it is ViewGroup)
          it.removeView(inputView)
      }
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

    confirmButton.text = positiveButtonText
    drawableConfirmButtonResId?.let {
      confirmButton.setCompoundDrawablesWithIntrinsicBounds(
        0,
        0,
        it,
        0
      )
    }
    confirmButton.setOnClickListener {
      positiveAction(dialog, editText.text.toString())
    }
    negativeButton.setVisible(negativeAction != null)
    negativeButton.text = negativeButtonText

    negativeButton.setOnClickListener {
      negativeAction.let {
        it?.invoke(dialog)
      }
      if (negativeAction == null)
        dialog.hide()
    }

    btnClose.setOnClickListener {
      closeAction?.let { it.invoke(dialog) }
      dialog.hide()
    }

    viewsBeforeInput.forEach { layoutBeforeInput.addView(it) }
    viewsAfterInput.forEach { layoutAfterInput.addView(it) }

    addViewsFn(viewsAfterInputFn, dialog, layoutAfterInput)
    addViewsFn(viewsBeforeInputFn, dialog, layoutBeforeInput)

    var editToShowKeyboard: EditText = editText
    if (inputView != null) {
      if (inputView is NumberInput) {
        editToShowKeyboard = inputView.custom_number_input
        inputView.addOnDoneListener { result ->
          positiveAction(dialog, result)
        }
      }
    }
    editToShowKeyboard.setKeyboardVisible(this)
    layout
  }

  val newDialog = dialog ?: Dialog(this, R.style.full_screen_dialog)
  val layout = createLayoutFn(newDialog)
  newDialog.setContentView(layout)
  if (dialog == null) {
    newDialog.show()
  }
  return createLayoutFn
}

private fun addViewsFn(
  viewsAfterInputFn: (dialog: Dialog, views: MutableList<View>) -> Any,
  dialog: Dialog,
  layoutAfterInput: LinearLayout
) {
  val views = mutableListOf<View>()
  viewsAfterInputFn(dialog, views)
  views.forEach { layoutAfterInput.addView(it) }
}


