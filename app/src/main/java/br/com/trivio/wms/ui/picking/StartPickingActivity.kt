package br.com.trivio.wms.ui.picking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.formatTo
import br.com.trivio.wms.extensions.setLoading
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.equipments.AddEquipmentsActivity
import br.com.trivio.wms.viewmodel.picking.PickingViewModel
import kotlinx.android.synthetic.main.activity_start_picking.*
import kotlinx.android.synthetic.main.button_close_x.*

class StartPickingActivity : MyAppCompatActivity() {

  private val pickingViewModel: PickingViewModel by viewModels()
  private var taskId: Long = 0

  companion object {
    const val PICKING_TASK_ID = "PICKING_TASK_ID"
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.taskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    setContentView(R.layout.activity_start_picking)
    setupObservables()
    listenClickEvents()
    listenRefresh()
    loadPickingDetails()
  }

  override fun onResume() {
    super.onResume()
    loadPickingDetails()
  }

  private fun setupObservables() {
    pickingViewModel.task.observe(this, { result ->
      onResult(
        result,
        onSuccess = {
          updateUi(it.data)
        },
        always = {
          setInputsLoading(false)
          refresh_start_picking_activity.isRefreshing = false
        },
      )
    })
  }

  private fun setInputsLoading(loading: Boolean) {
    cargo_info.loading = loading
    customer_name_info.loading = loading
    emited_at_info.loading = loading
    seller_info.loading = loading
    qtd_items_to_pick.setLoading(loading)
    qtd_picked_items.setLoading(loading)
  }

  private fun updateUi(data: PickingTaskDto) {
    order_number.text = data.orderNumber.toString()
    customer_name_info.text = data.customerName
    emited_at_info.text = data.orderDate?.formatTo("dd/MM/yyyy")
    seller_info.text = "${data.sellerCode} - ${data.sellerName}"
    qtd_picked_items.text = data.quantityPickedItems.toString()
    layout_qtd_itens_to_pick.setVisible(data.taskStatus == TaskStatus.DOING)
    qtd_items_to_pick.text = data.quantityItemsToPick.toString()
    btn_choose_equipments.setVisible(data.taskStatus == TaskStatus.PENDING)
    btn_continue_picking.setVisible(data.taskStatus == TaskStatus.DOING)
  }

  private fun loadPickingDetails() {
    setInputsLoading(true)
    Log.i("PICKING", "loadPickingDetails: $taskId")
    pickingViewModel.loadPickingTask(taskId)
  }

  private fun listenClickEvents() {
    btn_icon_x.setOnClickListener { finish() }
    btn_choose_equipments.setOnClickListener {
      openEquipmentsActivity()
    }
    btn_continue_picking.setOnClickListener {
      //openPickingActivity()
    }
  }

  private fun openEquipmentsActivity() {
    val chooseEquipmentsIntent = Intent(this, AddEquipmentsActivity::class.java)
    startActivity(chooseEquipmentsIntent)
  }

  private fun listenRefresh() {
    refresh_start_picking_activity.setOnRefreshListener {
      loadPickingDetails()
    }
  }

  private fun openPickingActivity() {
    /*val intent = Intent(this, CargoConferenceActivity::class.java)
    intent.putExtra(CargoConferenceActivity.CARGO_TASK_ID, taskId)
    startActivityForResult(intent, CargoConferenceActivity.RESULT_TASK_ID)*/
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)/*
    if (resultCode == CargoConferenceActivity.RESULT_TASK_ID) {
      data?.let {
        this.taskId = it.getLongExtra(CargoConferenceActivity.CARGO_TASK_ID, 0)
        this.loadPickingDetails()
      }
    }*/
  }
}
