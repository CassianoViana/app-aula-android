package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import br.com.trivio.wms.R

class SearchInput @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  private var editTextSearch: TextView

  fun addTextChangedListener(listener: (String) -> Unit) {
    editTextSearch.addTextChangedListener {
      listener(editTextSearch.text.toString())
    }
  }

  fun setText(search: String) {
    editTextSearch.text = search
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_search_input, this, true)
    editTextSearch = findViewById<EditText>(R.id.input_search)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.SearchInput)
      val hint = styledAttributes.getString(R.styleable.SearchInput_hint)
      editTextSearch.hint = hint
      styledAttributes.recycle()
    }

  }

}
