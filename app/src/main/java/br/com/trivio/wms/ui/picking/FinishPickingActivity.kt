package br.com.trivio.wms.ui.picking

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.Alert
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.extensions.setLoading
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.onResult
import br.com.trivio.wms.viewmodel.picking.PickingViewModel
import kotlinx.android.synthetic.main.activity_finish_picking.*
import kotlinx.android.synthetic.main.button_close_x.*

class FinishPickingActivity : MyAppCompatActivity() {

  private var taskId: Long = 0

  private val viewModel: PickingViewModel by viewModels()
  private var equipmentsAdapter = EquipmentsUsedAdapter()

  companion object {
    const val PICKING_TASK_ID = "PICKING_ID"
  }


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_finish_picking)
    this.taskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    bindListAdapter()
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadPickingTaskDetails()
    /*layout_success.setVisible(false)
    layout_fail.setVisible(false)*/
  }

  private fun bindListAdapter() {
    used_equipments_list.setAdapter(equipmentsAdapter)
  }

  private fun setupObservables() {
    viewModel.task.observe(this, { result ->
      onResult(result,
        onSuccess = {
          taskId = it.data.id
          updateUi(it.data)
        },
        always = {
          setInputsLoading(false)
        }
      )
    })
  }

  private fun setInputsLoading(loading: Boolean) {
    label_info_picking_task.setLoading(loading)
    label_input_number_total.setLoading(loading)
    label_input_number_separeds.setLoading(loading)
    label_input_number_not_found.setLoading(loading)
    if (!loading) {
      used_equipments_list.setLoading(loading)
      refresh_picking_task.isRefreshing = loading
    }
  }

  private fun updateUi(data: PickingTaskDto) {
    label_info_picking_task.text = data.orderNumber.toString()
    label_input_number_total.value = data.quantityItems.toString()
    label_input_number_separeds.value = data.quantityPickedItems.toString()
    label_input_number_not_found.value = data.quantityNotFound.toString()
    alert_partial_picked_order.message =
      getString(R.string.partial_pick_final_message, data.quantityPartiallyPicked)
    setEquipmetnsListData(data.equipments)
    label_input_number_not_found.setVisible(data.quantityNotFound > 0)
    if (data.valid()) {
      alert_partial_picked_order.message = getString(R.string.all_items_were_picked)
      alert_partial_picked_order.setType(Alert.TYPE_SUCCESS)
    } else {
      alert_partial_picked_order.setVisible(data.quantityPartiallyPicked > 0)
    }
  }

  private fun setEquipmetnsListData(equipments: List<EquipmentDto>) {
    equipmentsAdapter.items = equipments
    equipmentsAdapter.notifyDataSetChanged()
  }

  private fun loadPickingTaskDetails() {
    setInputsLoading(true)
    viewModel.loadPickingTask(taskId)
  }

  private fun listenClickEvents() {
    /*btn_restart_counting.setOnClickListener {
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
    }*/
    btn_icon_x.setOnClickListener { finish() }
  }

  private fun backToScreenThatInitiatedTheProcess() {
    clearTop()
  }

  private fun listenRefresh() {
    refresh_picking_task.setOnRefreshListener {
      loadPickingTaskDetails()
    }
  }

  class EquipmentsUsedAdapter : RecyclerView.Adapter<EquipmentAdapterViewHolder>() {
    var items: List<EquipmentDto> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentAdapterViewHolder {
      return EquipmentAdapterViewHolder(parent.inflateToViewHolder(R.layout.item_equipment_finish_activity))
    }

    override fun onBindViewHolder(holder: EquipmentAdapterViewHolder, position: Int) {
      holder.bind(items[position])
    }

    override fun getItemCount() = items.size

  }

  class EquipmentAdapterViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var equipmentNameTextView = view.findViewById<TextView>(R.id.equipment_name)
    var equipmentCodeTextView = view.findViewById<TextView>(R.id.equipment_code)

    fun bind(dto: EquipmentDto) {
      equipmentCodeTextView.text = dto.code
      equipmentNameTextView.text = dto.name
    }
  }
}
