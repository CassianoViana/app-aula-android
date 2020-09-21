package br.com.trivio.wms.ui.cargos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.formatTo
import br.com.trivio.wms.extensions.setLoading
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.cargos.conference.CargoConferenceActivity
import br.com.trivio.wms.viewmodel.cargo.CargoDetailsViewModel
import br.com.trivio.wms.viewmodel.cargo.conference.CargoConferenceViewModel
import kotlinx.android.synthetic.main.activity_start_conference.*
import kotlinx.android.synthetic.main.button_close_x.*

class StartConferenceActivity : MyAppCompatActivity() {

  private val cargoDetailsViewModel: CargoDetailsViewModel by viewModels()
  private val cargoConferenceViewModel: CargoConferenceViewModel by viewModels()
  private var taskId: Long = 0

  companion object {
    const val CARGO_TASK_ID = "CARGO_TASK_ID"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.taskId = intent.getLongExtra(CARGO_TASK_ID, 0)
    setContentView(R.layout.activity_start_conference)
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadCargoDetails()
  }

  override fun onResume() {
    super.onResume()
    loadCargoDetails()
  }

  private fun setupObservables() {
    cargoDetailsViewModel.cargoResult.observe(this, Observer { result ->
      onResult(result,
        onSuccess = {
          updateUi(it.data)
        },
        always = {
          setInputsLoading(false)
//          refresh_cargo_activity.isRefreshing = false
        }
      )
    })
  }

  private fun setInputsLoading(loading: Boolean) {
    driver_name_info.loading = loading
    truck_label_info.loading = loading
    company_name_info.loading = loading
    start_date_info.loading = loading
    bono_reference_code.setLoading(loading)
    qtd_counted_items.setLoading(loading)
    qtd_items_to_count.setLoading(loading)
  }

  private fun updateUi(data: CargoConferenceDto) {
    bono_reference_code.text = data.cargoReferenceCode
    truck_label_info.text = data.truckLabel
    driver_name_info.text = data.driverName
    company_name_info.text = data.nfesCompanyNames.joinToString("", transform = { "$it\n" })
    start_date_info.text = data.scheduledStart?.formatTo("dd/MM/yyyy - HH:mm").toString()
    progress_area_counting.setVisible(data.progressDescription != null)
    qtd_counted_items.value = data.totalCountedItems.toString()
    qtd_items_to_count.value = data.totalToCountItems.toString()
    btn_start_counting.setText(
      when (data.taskStatus) {
        TaskStatus.DOING -> R.string.continue_counting
        else -> R.string.start_counting
      }
    )
  }

  private fun loadCargoDetails() {
    setInputsLoading(true)
    val cargoId = intent.getLongExtra(CARGO_TASK_ID, 0)
    Log.i("CARGO", "loadingCargoDetails: $cargoId")
    cargoDetailsViewModel.loadCargo(cargoId)
  }

  private fun listenClickEvents() {
    btn_start_counting.setOnClickListener {
      cargoConferenceViewModel.startCounting(taskId) { result ->
        onResult(
          result,
          onSuccess = {
            openConferenceActivity()
          }
        )
      }
    }
    btn_icon_x.setOnClickListener { finish() }
  }

  private fun listenRefresh() {
//    refresh_cargo_activity.setOnRefreshListener {
//      loadCargoDetails()
//    }
  }

  private fun openConferenceActivity() {
    val intent = Intent(this, CargoConferenceActivity::class.java)
    intent.putExtra(CargoConferenceActivity.CARGO_TASK_ID, taskId)
    startActivityForResult(intent, CargoConferenceActivity.RESULT_TASK_ID)
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    if (resultCode == CargoConferenceActivity.RESULT_TASK_ID) {
      data?.let {
        this.taskId = it.getLongExtra(CargoConferenceActivity.CARGO_TASK_ID, 0)
        this.loadCargoDetails()
      }
    }
  }
}
