package br.com.trivio.wms.ui.equipments

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import br.com.trivio.wms.extensions.showMessageSuccess
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.picking.PickingActivity
import br.com.trivio.wms.viewmodel.EquipmentsViewModel
import br.com.trivio.wms.viewmodel.picking.PickingViewModel
import kotlinx.android.synthetic.main.activity_confirm_equipments_list.*

class ConfirmEquipmentsListActivity : MyAppCompatActivity() {

  private val viewModel: PickingViewModel by viewModels()
  private val equipmentViewModel: EquipmentsViewModel by viewModels()
  private var taskId: Long = 0

  companion object {
    const val PICKING_TASK_ID = "PICKING_TASK_ID"
  }

  private val adapter = EquipmentsListAdapter(onClickToRemove = { item ->
    removeEquipment(item)
  })

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    this.taskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    setContentView(R.layout.activity_confirm_equipments_list)
    observeViewModel()
    onRefreshReloadList()
    bindListAdapter()
    onClickToAddMoreThenGoToAddEquipmentsActivity()
    onClickContinueOpenToPickingActivity()
    loadTask()
  }

  private fun onClickToAddMoreThenGoToAddEquipmentsActivity() {
    btn_add_more.setOnClickListener {
      val intent = Intent(this, AddEquipmentsListActivity::class.java)
      intent.putExtra(AddEquipmentsListActivity.PICKING_TASK_ID, taskId)
      intent.putExtra(AddEquipmentsListActivity.MUTED, true)
      finish()
      startActivity(intent)
    }
  }

  private fun onClickContinueOpenToPickingActivity() {
    btn_continue_to_picking.setOnClickListener {
      openPickingActivity()
    }
  }

  private fun openPickingActivity() {
    val pickingIntent = Intent(this, PickingActivity::class.java)
    pickingIntent.putExtra(PickingActivity.PICKING_TASK_ID, taskId)
    startActivity(pickingIntent)
  }

  private fun removeEquipment(equipment: EquipmentDto) {
    AlertDialog.Builder(this)
      .setTitle(R.string.confirm_remove_equipment)
      .setView(inflate<View>(R.layout.item_equipment_remove).apply {
        findViewById<TextView>(R.id.equipment_code).text = equipment.code
        findViewById<TextView>(R.id.equipment_name).text = equipment.name
        findViewById<View>(R.id.btn_remove_equipment).setVisible(false)
      })
      .setPositiveButton(R.string.delete) { _, _ ->
        equipmentViewModel.removeEquipment(equipment) { result ->
          onResult(result,
            onSuccess = {
              showMessageSuccess(R.string.success_on_remove_equipment)
              adapter.removeEquipment(equipment)
            }
          )
        }
      }
      .setNegativeButton(R.string.cancel) { _, _ -> }
      .create()
      .show()
  }

  private fun onRefreshReloadList() {
    equipments_list.setOnRefreshListener {
      loadTask()
    }
  }

  private fun bindListAdapter() {
    equipments_list.setAdapter(adapter)
  }

  private fun loadTask() {
    viewModel.loadPickingTask(taskId)
  }

  private fun observeViewModel() {
    viewModel.task.observe(this, { result ->
      onResult(result,
        onSuccess = {
          setEquipments(it.data.equipments)
        },
        always = {
          equipments_list.stopRefresh()
        }
      )
    })
  }

  private fun setEquipments(data: List<EquipmentDto>) {
    adapter.items = data.toMutableList()
  }

  class EquipmentsListAdapter(val onClickToRemove: (EquipmentDto) -> Unit) :
    RecyclerView.Adapter<EquipmentListViewHolder>() {
    var items: MutableList<EquipmentDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EquipmentListViewHolder {
      return EquipmentListViewHolder(parent.inflateToViewHolder(R.layout.item_equipment_remove))
    }

    override fun onBindViewHolder(holder: EquipmentListViewHolder, position: Int) {
      holder.bind(items[position], onClickToRemove)
    }

    override fun getItemCount() = items.size
    fun removeEquipment(equipment: EquipmentDto) {
      items.remove(equipment)
      notifyDataSetChanged()
    }
  }

  class EquipmentListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var equipmentNameTextView = view.findViewById<TextView>(R.id.equipment_name)
    var equipmentCodeTextView = view.findViewById<TextView>(R.id.equipment_code)
    var removeEquipmentBtn = view.findViewById<View>(R.id.btn_remove_equipment)

    fun bind(dto: EquipmentDto, onClickToRemove: (EquipmentDto) -> Unit) {
      equipmentNameTextView.text = dto.name
      equipmentCodeTextView.text = dto.code
      removeEquipmentBtn.setOnClickListener {
        onClickToRemove(dto)
      }
    }
  }
}
