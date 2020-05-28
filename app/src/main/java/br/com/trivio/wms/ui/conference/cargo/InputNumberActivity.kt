package br.com.trivio.wms.ui.conference.cargo

import android.os.Bundle
import android.widget.EditText
import br.com.trivio.wms.R
import br.com.trivio.wms.showKeyboard
import br.com.trivio.wms.MyAppCompatActivity

class InputNumberActivity : MyAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_input_number)
    val inputValue: EditText = findViewById(R.id.input_value)
    showKeyboard(inputValue)
  }
}
