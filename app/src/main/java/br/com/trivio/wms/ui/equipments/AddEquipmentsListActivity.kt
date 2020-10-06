package br.com.trivio.wms.ui.equipments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.EquipmentDto
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.extensions.toggleVisibility
import br.com.trivio.wms.onResult
import br.com.trivio.wms.viewmodel.EquipmentsViewModel
import kotlinx.android.synthetic.main.activity_add_equipments_list.*

class AddEquipmentsListActivity : MyAppCompatActivity() {

  private val viewModel: EquipmentsViewModel by viewModels()
  private var taskId: Long = 0

  companion object {
    const val PICKING_TASK_ID = "PICKING_TASK_ID"
    const val MUTED = "MUTED"
  }

  private val adapter = EquipmentsListAdapter(onClickToSelect = { item ->
    addEquipment(item)
  })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.taskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    setContentView(R.layout.activity_add_equipments_list)
    observeViewModel()
    onRefreshReloadList()
    bindListAdapter()
    onBarcodeChangeFilter()
    onClickBarcodeThenShowBarcodeReader()
    onClickContinueOpenToPickingActivity()
    if (!intent.getBooleanExtra(MUTED, false))
      say(getString(R.string.message_select_picking_equipments))
  }

  override fun onResume() {
    super.onResume()
    loadEquipments()
  }

  private fun onBarcodeChangeFilter() {
    search_input_equipments.addOnTextChangeListener {
      setEquipments(viewModel.filter(it))
    }
  }

  private fun onClickContinueOpenToPickingActivity() {
    btn_continue_to_picking.setOnClickListener {
      viewModel.idsEquipmentsToAdd.value?.let { equipmentIdsToAdd ->
        viewModel.addEquipmentsByIds(equipmentIdsToAdd, taskId) { result ->
          onResult(result,
            onSuccess = {
              openConfirmEquipmentsActivity()
            })
        }
      }
    }
  }

  private fun openConfirmEquipmentsActivity() {
    val confirmEquipmentsIntent = Intent(this, ConfirmEquipmentsListActivity::class.java)
    confirmEquipmentsIntent.putExtra(ConfirmEquipmentsListActivity.PICKING_TASK_ID, taskId)
    startActivity(confirmEquipmentsIntent)
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

  private fun addEquipment(equipment: EquipmentDto) {
    viewModel.toggleAddEquipment(equipment)
    adapter.notifyDataSetChanged()
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
    viewModel.idsEquipmentsToAdd.observe(this, {
      updateLabelQtdSelecteds(it.size)
    })
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
    adapter.items = data.sortedBy { it.name }
  }

  private fun updateLabelQtdSelecteds(totalToAdd: Int) {
    totalToAdd.apply {
      btn_continue_to_picking.text = when {
        this > 0 -> getString(R.string.add_parentesis, this)
        else -> getString(R.string.add)
      }
    }
  }

  class EquipmentsListAdapter(val onClickToSelect: (EquipmentDto) -> Unit) :
    RecyclerView.Adapter<EquipmentListViewHolder>() {
    var items: List<EquipmentDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentListViewHolder {
      return EquipmentListViewHolder(parent.inflateToViewHolder(R.layout.item_equipment_add))
    }

    override fun onBindViewHolder(holder: EquipmentListViewHolder, position: Int) {
      holder.bind(items[position], onClickToSelect)
    }

    override fun getItemCount() = items.size
  }

  class EquipmentListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var equipmentNameTextView = view.findViewById<TextView>(R.id.equipment_name)
    var equipmentCodeTextView = view.findViewById<TextView>(R.id.equipment_code)
    var btnSelectEquipment = view.findViewById<Button>(R.id.btn_add_equipment)

    fun bind(dto: EquipmentDto, onClickToSelect: (EquipmentDto) -> Unit) {
      equipmentNameTextView.text = dto.name
      equipmentCodeTextView.text = dto.code
      btnSelectEquipment.apply {
        text = view.context.getString(
          if (dto.selected) {
            R.string.remove
          } else {
            R.string.select
          }
        )
        background = view.context.getDrawable(
          if (dto.selected) {
            R.drawable.light_red_rounded
          } else {
            R.drawable.light_blue_rounded
          }
        )
      }
      btnSelectEquipment.setOnClickListener {
        onClickToSelect(dto)
      }
    }
  }
}
