package br.com.trivio.wms.ui.cargos

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.extensions.Status
import br.com.trivio.wms.extensions.setLoading
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.threatResult
import kotlinx.android.synthetic.main.activity_end_conference.*
import kotlinx.android.synthetic.main.button_close_x.*

class EndConferenceActivity : MyAppCompatActivity() {

  private val cargoDetailsViewModel: CargoDetailsViewModel by viewModels()

  companion object {
    const val CARGO_ID = "CARGO_ID"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_end_conference)
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadCargoDetails()
  }

  private fun setupObservables() {
    cargoDetailsViewModel.cargoResult.observe(this, Observer { result ->
      threatResult(result,
        onSuccess = {
          updateUi(it.data)
        },
        always = {
          setInputsLoading(false)
          refresh_cargo_activity.isRefreshing = false
        }
      )
    })
  }

  private fun setInputsLoading(loading: Boolean) {
    driver_name_info.setLoading(loading)
    truck_label_info.setLoading(loading)
    bono_ref_code_info.setLoading(loading)
    final_message.setLoading(loading)
    icon_finish_counting.setLoading(loading)
  }

  private fun updateUi(data: CargoConferenceDto) {
    bono_ref_code_info.text = getString(R.string.bono_ref_code_template, data.cargoReferenceCode)
    truck_label_info.text = data.truckLabel
    driver_name_info.text = data.driverName
    final_message.text = getString(
      when {
        data.isFinishedWithAllCorrect() -> R.string.all_correct_bono_counted
        else -> R.string.not_all_correct_bono_counted
      }
    )
    icon_finish_counting.setImageResource(
      when {
        data.isFinishedWithAllCorrect() -> Status.SUCCESS.bigIcon
        else -> Status.ERROR.bigIcon
      }
    )
    layout_success.setVisible(data.isFinishedWithAllCorrect())
    layout_fail.setVisible(!data.isFinishedWithAllCorrect())
  }

  private fun loadCargoDetails() {
    setInputsLoading(true)
    val cargoId = intent.getLongExtra(CARGO_ID, 0)
    Log.i("CARGO", "loadingCargoDetails: $cargoId")
    cargoDetailsViewModel.loadCargo(cargoId, true)
  }

  private fun listenClickEvents() {
    btn_finish.setOnClickListener {
    }
    btn_icon_x.setOnClickListener { finish() }
    btn_cancel.setOnClickListener { finish() }
  }

  private fun listenRefresh() {
    refresh_cargo_activity.setOnRefreshListener {
      loadCargoDetails()
    }
  }
}
