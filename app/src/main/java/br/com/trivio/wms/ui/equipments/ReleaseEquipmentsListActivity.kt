package br.com.trivio.wms.ui.equipments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.extensions.inflate
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.extensions.toggleVisibility
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.picking.FinishPickingActivity
import br.com.trivio.wms.viewmodel.EquipmentsViewModel
import kotlinx.android.synthetic.main.activity_release_equipments_list.*

class ReleaseEquipmentsListActivity : MyAppCompatActivity() {
  private val viewModel: EquipmentsViewModel by viewModels()
  private var taskId: Long = 0

  companion object {
    const val PICKING_TASK_ID = "PICKING_TASK_ID"
    const val END_ACTIVITY = 100
  }

  private val adapter = EquipmentsListAdapter(onClickToRemoveEquipment = { item ->
    removeEquipment(item)
  })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.taskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    setContentView(R.layout.activity_release_equipments_list)
    observeViewModel()
    onRefreshReloadList()
    bindListAdapter()
    loadEquipments()
    setBarcodeReaderVisible(false)
    onClickBarcodeThenShowBarcodeReader()
    onClickContinueOpenFinishPickingActivity()
  }

  private fun onClickContinueOpenFinishPickingActivity() {
    btn_continue_to_finish_picking.setOnClickListener {
      openFinishPickingActivity()
    }
  }

  private fun openFinishPickingActivity() {
    val finishPickingActivity = Intent(this, FinishPickingActivity::class.java)
    finishPickingActivity.putExtra(FinishPickingActivity.PICKING_TASK_ID, taskId)
    startActivity(finishPickingActivity)
  }

  private fun onClickBarcodeThenShowBarcodeReader() {
    btn_open_camera_to_read_equipment.setOnClickListener {
      if (barcode_equipments_reader.toggleVisibility() == View.VISIBLE) {
        barcode_equipments_reader.startRead()
      } else {
        barcode_equipments_reader.stopReading()
      }
    }
  }

  private fun setBarcodeReaderVisible(visible: Boolean = true) {
    barcode_equipments_reader.setVisible(visible)
  }

  private fun removeEquipment(item: EquipmentDto) {

    AlertDialog.Builder(this)
      .setTitle(R.string.confirm_remove_equipment)
      .setView(inflate<View>(R.layout.item_equipment_add).apply {
        findViewById<TextView>(R.id.equipment_code).text = item.code
        findViewById<TextView>(R.id.equipment_name).text = item.name
        findViewById<TextView>(R.id.equipment_position).setVisible(false)
      })
      .setPositiveButton(R.string.delete) { _, _ ->

      }
      .setNegativeButton(R.string.cancel, { _, _ -> })
      .create()
      .show()
  }

  private fun onRefreshReloadList() {
    equipments_list.setOnRefreshListener {
      loadEquipments()
    }
  }

  private fun bindListAdapter() {
    equipments_list.setAdapter(adapter)
  }

  private fun loadEquipments() {
    viewModel.loadEquipments(taskId)
  }

  private fun observeViewModel() {
    viewModel.availableEquipments.observe(this, {
      onResult(it,
        onSuccess = {
          setEquipments(it.data)
        },
        always = {
          equipments_list.stopRefresh()
          endLoading()
        }
      )
    })
  }

  private fun setEquipments(data: List<EquipmentDto>) {
    adapter.items = data
  }

  class EquipmentsListAdapter(val onClickToRemoveEquipment: (EquipmentDto) -> Unit) :
    RecyclerView.Adapter<EquipmentListViewHolder>() {
    var items: List<EquipmentDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentListViewHolder {
      return EquipmentListViewHolder(parent.inflateToViewHolder(R.layout.item_equipment_release))
    }

    override fun onBindViewHolder(holder: EquipmentListViewHolder, position: Int) {
      holder.bind(items[position], onClickToRemoveEquipment, position)
    }

    override fun getItemCount() = items.size
  }

  class EquipmentListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var equipmentNameTextView = view.findViewById<TextView>(R.id.equipment_name)
    var equipmentCodeTextView = view.findViewById<TextView>(R.id.equipment_code)
    var equipmentPosition = view.findViewById<TextView>(R.id.equipment_position)
    var imgSuccessfullyValidated =
      view.findViewById<ImageView>(R.id.img_equipment_successfully_validated)

    init {

    }

    fun bind(dto: EquipmentDto, onClickToRemove: (EquipmentDto) -> Unit, position: Int) {
      imgSuccessfullyValidated.setVisible(true)

      equipmentPosition.text = (position + 1).toString()
      equipmentNameTextView.text = dto.name
      equipmentCodeTextView.text = dto.code
    }

  }
}