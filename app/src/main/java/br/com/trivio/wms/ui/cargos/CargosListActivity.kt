package br.com.trivio.wms.ui.cargos

import android.content.Intent
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
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.onResult
import br.com.trivio.wms.viewmodel.cargo.CargoListViewModel
import kotlinx.android.synthetic.main.activity_cargos_list.*

class CargosListActivity : MyAppCompatActivity() {

  private val viewModel: CargoListViewModel by viewModels()

  private val adapter = CargosAdapter { cargoItemClicked: CargoListDto ->
    openStartConferenceActivity(cargoItemClicked)
  }

  private fun openStartConferenceActivity(cargoItemClicked: CargoListDto) {
    val intent = Intent(this@CargosListActivity, StartConferenceActivity::class.java)
    intent.putExtra(StartConferenceActivity.CARGO_TASK_ID, cargoItemClicked.taskId)
    startActivity(intent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    startTask()
    setContentView(R.layout.activity_cargos_list)
    observeViewModel()
    bindListAdapter()
    addRefreshListener()
    onSearchFilterItems()
  }

  private fun onSearchFilterItems() {
    search_input_cargos.addOnTextChangeListener {
      setCargos(viewModel.filter(it))
    }
  }

  override fun onResume() {
    super.onResume()
    loadPendingCargos()
  }

  private fun bindListAdapter() {
    cargos_list.setAdapter(adapter)
  }

  private fun addRefreshListener() {
    cargos_list.setOnRefreshListener {
      viewModel.loadCargos()
    }
  }

  private fun observeViewModel() {
    viewModel.cargosResult.observe(this, {
      onResult(it,
        onSuccess = { success ->
          setCargos(success.data)
        },
        always = {
          endLoading()
          cargos_list.stopRefresh()
        }
      )
    })
  }

  fun setCargos(cargos: List<CargoListDto>) {
    adapter.cargos = cargos
    cargos_list.showEmptyLabel(cargos.isEmpty())
  }

  private fun loadPendingCargos() {
    startLoading()
    viewModel.loadCargos()
  }

  class CargosAdapter(
    private
    val onClickListener: (cargoListDto: CargoListDto) -> Any
  ) :
    RecyclerView.Adapter<CargosAdapter.ViewHolder>() {

    var cargos: List<CargoListDto> = mutableListOf()
      set(value) {
        field = value.sortedByDescending { cargo -> cargo.scheduledStart }
        notifyDataSetChanged()
      }

    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout) {
      private var cargoNameText: TextView = layout.findViewById(R.id.ped_cli_txt_view)
      private var cargoRefCode: TextView = layout.findViewById(R.id.cargo_txt_view)
      private var cargoStatus: Badge = layout.findViewById(R.id.cargo_status_badge)
      private var progressBar: ProgressBar = layout.findViewById(R.id.progress_bar)
      private var quantityToCount: TextView =
        layout.findViewById(R.id.text_view_quantity_to_count)
      private var cargoDate: TextView = layout.findViewById(R.id.item_date)

      fun bind(
        cargoListDto: CargoListDto,
        onClickListener: (cargoListDto: CargoListDto) -> Any
      ) {
        cargoListDto.apply {
          cargoNameText.text = toString()
          cargoRefCode.text = bonoName()
          cargoDate.text = formattedScheduledStart()
          cargoStatus.text = cargoListDto.cargoStatusDto?.name
          cargoStatus.backgroundColor = cargoListDto.cargoStatusDto?.color
          quantityToCount.text = quantityItemsToCount.toString()
          progressBar.setProgress(cargoListDto.progress)
          layout.setOnClickListener {
            onClickListener(this)
          }
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      return ViewHolder(parent.inflateToViewHolder(R.layout.item_cargo_layout))
    }

    override fun getItemCount() = cargos.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      holder.bind(cargos[position], onClickListener)
    }
  }
}
