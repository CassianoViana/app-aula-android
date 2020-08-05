package br.com.trivio.wms.ui.cargos

import android.content.Intent
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
import br.com.trivio.wms.extensions.showMessageSuccess
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.conference.cargo.CargoConferenceViewModel
import kotlinx.android.synthetic.main.activity_end_conference.*
import kotlinx.android.synthetic.main.button_close_x.*

class EndConferenceActivity : MyAppCompatActivity() {

  private var cargoId: Long = 0
  private var taskId: Long = 0
  private val cargoDetailsViewModel: CargoDetailsViewModel by viewModels()
  private val cargoConferenceViewModel: CargoConferenceViewModel by viewModels()

  companion object {
    const val CARGO_TASK_ID = "CARGO_ID"
    const val RESTARTING_TASK = "RESTARTING_TASK"
    const val END_CONFERENCE_ACTIVITY = 1
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_end_conference)
    this.cargoId = intent.getLongExtra(CARGO_TASK_ID, 0)
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadCargoDetails()
  }

  private fun setupObservables() {
    cargoDetailsViewModel.cargoResult.observe(this, Observer { result ->
      onResult(result,
        onSuccess = {
          taskId = it.data.taskId
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
    Log.i("CARGO", "loadingCargoDetails: $cargoId")
    cargoDetailsViewModel.loadCargo(cargoId, true)
  }

  private fun listenClickEvents() {
    btn_restart_counting.setOnClickListener {
      cargoConferenceViewModel.restartCounting(taskId) { result ->
        onResult(
          result,
          onSuccess = {
            showMessageSuccess(R.string.the_counting_was_restarted)
            val restartingData = Intent()
            restartingData.putExtra(RESTARTING_TASK, it.data.taskId);
            setResult(END_CONFERENCE_ACTIVITY, restartingData)
            finish()
          }
        )
      }
    }
    btn_finish_with_divergences.setOnClickListener {
      cargoConferenceViewModel.finishCounting(taskId) { result ->
        onResult(
          result,
          onSuccess = {
            showMessageSuccess(R.string.the_counting_was_finished)
            finish()
          }
        )
      }
    }
    btn_finish_counting.setOnClickListener {
      //cargoDetailsViewModel.
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
