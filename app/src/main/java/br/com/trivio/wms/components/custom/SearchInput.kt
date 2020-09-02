package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout
import androidx.core.widget.addTextChangedListener
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.hideKeyboard
import br.com.trivio.wms.extensions.setKeyboardVisible
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_search_input.view.*

class SearchInput @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var onSearchListener: (String) -> Unit = {}

  val text: String?
    get() = custom_input_search.text.toString()

  fun addOnSearchListener(listener: (String) -> Unit) {
    this.onSearchListener = listener;
    custom_input_search.setOnEditorActionListener { textView, actionId, keyEvent ->
      if (actionId == EditorInfo.IME_ACTION_SEARCH) {
        notifySearch()
      }
      true
    }
  }

  private fun notifySearch() {
    this.onSearchListener(custom_input_search.text.toString())
  }

  fun applySearch(search: String) {
    setText(search)
    notifySearch()
  }

  fun setText(search: String) {
    custom_input_search.setText(search)
  }

  fun setKeyboardVisible(b: Boolean) {
    custom_input_search.setKeyboardVisible(context, b)
  }

  fun reset() {
    custom_input_search.clearComposingText()
    custom_input_search.setKeyboardVisible(context, false)
  }

  fun hideKeyboard() {
    (context as MyAppCompatActivity).hideKeyboard(custom_input_search)
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_search_input, this, true)

    icon_clear.setOnClickListener {
      setText("")
    }
    custom_input_search.addTextChangedListener {
      icon_clear.setVisible(custom_input_search.text.toString().isNotEmpty())
    }

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.SearchInput)
      val hint = styledAttributes.getString(R.styleable.SearchInput_hint)
      custom_input_search.hint = hint
      styledAttributes.recycle()
    }

  }

}
