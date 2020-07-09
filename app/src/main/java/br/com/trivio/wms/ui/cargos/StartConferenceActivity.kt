package br.com.trivio.wms.ui.cargos

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.threatResult

class StartConferenceActivity : MyAppCompatActivity() {

  lateinit var btnStart: Button
  lateinit var btnCancel: Button
  private val cargoDetailsViewModel: CargoDetailsViewModel by viewModels()

  companion object {
    const val CARGO_ID = "CARGO_ID"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_start_conference)
    bindViews()
    setupObservables()
    listenClickEvents()
    loadCargosDetails()
  }

  private fun setupObservables() {
    cargoDetailsViewModel.cargoResult.observe(this, Observer { result ->
      threatResult(result,
        onSuccess = {
          updateUi(it.data)
        })
    })
  }

  private fun updateUi(data: CargoConferenceDto) {
    finish()
  }

  private fun loadCargosDetails() {
    val cargoId = intent.getLongExtra(CARGO_ID, 0)
    Log.i("CARGO", "loadingCargoDetails: $cargoId")
    cargoDetailsViewModel.loadCargo(cargoId)
  }

  private fun bindViews() {
    btnStart = findViewById(R.id.btn_start)
    btnCancel = findViewById(R.id.btn_cancel)
  }

  private fun listenClickEvents() {
    btnStart.setOnClickListener {

    }
    btnCancel.setOnClickListener {
      finish()
    }
  }
}
