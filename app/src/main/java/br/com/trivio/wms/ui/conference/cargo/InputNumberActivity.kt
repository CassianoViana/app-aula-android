package br.com.trivio.wms.ui.conference.cargo

import android.os.Bundle
import android.widget.EditText
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.showKeyboard

class InputNumberActivity : MyAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.custom_prompt)
    val inputValue: EditText = findViewById(R.id.input_value)
    showKeyboard(inputValue)
  }
}
