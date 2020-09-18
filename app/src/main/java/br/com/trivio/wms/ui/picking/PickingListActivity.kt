package br.com.trivio.wms.ui.picking

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.Badge
import br.com.trivio.wms.components.custom.ProgressBar
import br.com.trivio.wms.data.dto.PickingListDto
import br.com.trivio.wms.extensions.formatTo
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.extensions.setVisible
import br.com.trivio.wms.onResult
import br.com.trivio.wms.viewmodel.picking.PickingListViewModel
import kotlinx.android.synthetic.main.activity_picking_list.*

class PickingListActivity : MyAppCompatActivity() {

  private val viewModel: PickingListViewModel by viewModels()

  private val adapter = PickingListAdapter(onItemClick = { item ->
    openStartPickingActivity(item)
  })

  private fun openStartPickingActivity(item: PickingListDto) {
    val startPickingIntent = Intent(this, StartPickingActivity::class.java)
    startPickingIntent.putExtra(StartPickingActivity.PICKING_TASK_ID, item.id)
    startActivity(startPickingIntent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_picking_list)
    observeViewModel()
    onRefreshReloadList()
    bindListAdapter()
    loadPickings()
  }

  private fun onRefreshReloadList() {
    pickings_list.setOnRefreshListener {
      loadPickings()
    }
  }

  private fun bindListAdapter() {
    pickings_list.setAdapter(adapter)
  }

  private fun loadPickings() {
    viewModel.loadPickings()
  }

  private fun observeViewModel() {
    viewModel.pickingsResult.observe(this, {
      onResult(it,
        onSuccess = {
          setPickings(it.data)
        },
        always = {
          pickings_list.stopRefresh()
          endLoading()
        }
      )
    })
  }

  private fun setPickings(data: List<PickingListDto>) {
    adapter.items = data
  }

  class PickingListAdapter(val onItemClick: (PickingListDto) -> Unit) :
    RecyclerView.Adapter<PickingListViewHolder>() {
    var items: List<PickingListDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickingListViewHolder {
      return PickingListViewHolder(parent.inflateToViewHolder(R.layout.item_picking_tasks_list_layout))
    }

    override fun onBindViewHolder(holder: PickingListViewHolder, position: Int) {
      holder.bind(items[position], onItemClick)
    }

    override fun getItemCount() = items.size
  }

  class PickingListViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    var orderTitleTextView = view.findViewById<TextView>(R.id.ped_cli_txt_view)
    var cargoTextView = view.findViewById<TextView>(R.id.cargo_txt_view)
    var orderDateTextView = view.findViewById<TextView>(R.id.item_date)
    var priorityLabelTextView = view.findViewById<TextView>(R.id.prioritary_label)
    var qtdToPickTextView = view.findViewById<TextView>(R.id.quantity_to_pick_txt_view)
    var statusBadge = view.findViewById<Badge>(R.id.picking_task_badge)
    var progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
    fun bind(pickingListDto: PickingListDto, onItemClick: (PickingListDto) -> Unit) {

      view.setOnClickListener {
        onItemClick(pickingListDto)
      }

      orderTitleTextView.text = view.context.getString(
        R.string.item_picking_title,
        pickingListDto.orderNumber,
        pickingListDto.customerName
      )

      cargoTextView.text = view.context.getString(R.string.cargo_dot, pickingListDto.cargoNumber)
      orderDateTextView.text = pickingListDto.orderDate?.formatTo("dd/MM/yyyy")
      priorityLabelTextView.setVisible(pickingListDto.priorityStatus != null)

      pickingListDto.priorityStatus?.let {
        priorityLabelTextView.setTextColor(Color.parseColor(it.color))
        priorityLabelTextView.text = it.name
      }
      qtdToPickTextView.text = pickingListDto.quantityItems.toString()
      progressBar.setProgress(pickingListDto.progress)
      pickingListDto.status?.let {
        statusBadge.backgroundColor = it.color
        statusBadge.text = it.name
      }

    }

  }
}
