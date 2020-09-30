package br.com.trivio.wms.ui.cargos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.onResult
import br.com.trivio.wms.viewmodel.cargo.CargoDetailsViewModel
import br.com.trivio.wms.viewmodel.cargo.conference.CargoConferenceViewModel
import kotlinx.android.synthetic.main.activity_finish_conference.*
import kotlinx.android.synthetic.main.button_close_x.*

class FinishConferenceActivity : MyAppCompatActivity() {

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
    setContentView(R.layout.activity_finish_conference)
    this.cargoId = intent.getLongExtra(CARGO_TASK_ID, 0)
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadCargoDetails()
    layout_success.setVisible(false)
    layout_fail.setVisible(false)
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
        data.isValid() -> R.string.all_correct_bono_counted
        else -> R.string.not_all_correct_bono_counted
      }
    )
    icon_finish_counting.setImageResource(
      when {
        data.isValid() -> Status.SUCCESS.bigIcon
        else -> Status.ERROR.bigIcon
      }
    )
    layout_success.setVisible(data.isValid())
    layout_fail.setVisible(!data.isValid())
  }

  private fun loadCargoDetails() {
    setInputsLoading(true)
    Log.i("CARGO", "loadingCargoDetails: $cargoId")
    cargoDetailsViewModel.loadCargo(cargoId)
  }

  private fun listenClickEvents() {
    btn_restart_counting.setOnClickListener {
      startLoading(R.string.restarting_count)
      cargoConferenceViewModel.restartCounting(taskId) { result ->
        onResult(
          result,
          onSuccess = {
            showMessageSuccess(R.string.the_counting_was_restarted)
            val restartingData = Intent()
            restartingData.putExtra(RESTARTING_TASK, it.data.taskId)
            setResult(END_CONFERENCE_ACTIVITY, restartingData)
            finish()
          }
        )
      }
    }
    btn_finish_with_divergences.setOnClickListener {
      startLoading(R.string.finishing_count)
      cargoConferenceViewModel.finishCounting(taskId) { result ->
        onResult(
          result,
          onSuccess = {
            showMessageSuccess(R.string.the_counting_was_finished_with_divergences)
            backToScreenThatInitiatedTheProcess()
          }
        )
      }
    }
    btn_finish_counting.setOnClickListener {
      startLoading(R.string.finishing_count)
      cargoConferenceViewModel.finishCounting(taskId) {
        onResult(it, onSuccess = {
          showMessageSuccess(R.string.the_counting_was_finished)
          backToScreenThatInitiatedTheProcess()
        })
      }
    }
    btn_icon_x.setOnClickListener { finish() }
  }

  private fun backToScreenThatInitiatedTheProcess() {
    clearTop()
  }

  private fun listenRefresh() {
    refresh_cargo_activity.setOnRefreshListener {
      loadCargoDetails()
    }
  }
}