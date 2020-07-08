package br.com.trivio.wms.ui.cargos

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.RefreshableList
import br.com.trivio.wms.data.dto.CargoListDto
import br.com.trivio.wms.extensions.endLoading
import br.com.trivio.wms.extensions.setupToolbar
import br.com.trivio.wms.extensions.startLoading
import br.com.trivio.wms.inflateToViewHolder
import br.com.trivio.wms.threatResult
import br.com.trivio.wms.ui.tasks.TaskDetailsActivity

class CargosActivity : MyAppCompatActivity() {

  private val viewModel: CargoArrivalsViewModel by viewModels()
  private lateinit var loading: ProgressBar
  private lateinit var cargosList: RefreshableList

  private val adapter = CargosAdapter { cargoItemClicked: CargoListDto ->
    val intent = Intent(this@CargosActivity, TaskDetailsActivity::class.java)
    intent.putExtra(TaskDetailsActivity.TASK_ID, cargoItemClicked.taskId)
    startActivity(intent)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargos)
    setupToolbar()
    associateComponents()
    observeViewModel()
    bindListAdapter()
    addRefreshListener()
  }

  override fun onResume() {
    super.onResume()
    loadTasks()
  }

  private fun associateComponents() {
    cargosList = findViewById(R.id.list)
    loading = findViewById(R.id.progress_bar)
  }

  private fun bindListAdapter() {
    cargosList.setAdapter(adapter)
  }

  private fun addRefreshListener() {
    cargosList.setOnRefreshListener {
      viewModel.loadTasks()
    }
  }

  private fun observeViewModel() {
    viewModel.cargosResult.observe(this, Observer {
      threatResult(it,
        onSuccess = { success ->
          adapter.cargos = success.data
        },
        always = {
          endLoading()
          cargosList.stopRefresh()
        }
      )
    })
  }

  private fun loadTasks() {
    startLoading()
    viewModel.loadTasks()
  }

  class CargosAdapter(
    private val onClickListener: (cargoListDto: CargoListDto) -> Any
  ) :
    RecyclerView.Adapter<CargosAdapter.ViewHolder>() {

    var cargos: List<CargoListDto> = mutableListOf()
      set(value) {
        field = value.sortedByDescending { cargo -> cargo.scheduledStart }
        notifyDataSetChanged()
      }

    class ViewHolder(val layout: View) : RecyclerView.ViewHolder(layout) {
      private var cargoNameText: TextView = layout.findViewById(R.id.name_text)
      private var cargoRefCode: TextView = layout.findViewById(R.id.item_code)
      //private var cargoStatus: TextView = layout.findViewById(R.id.cargo_status)
      private var cargoDate: TextView = layout.findViewById(R.id.item_date)

      fun bind(
        cargoListDto: CargoListDto,
        onClickListener: (cargoListDto: CargoListDto) -> Any
      ) {
        cargoNameText.text = cargoListDto.toString()
        cargoRefCode.text = cargoListDto.bonoName()
        cargoDate.text = cargoListDto.formattedScheduledStart()
        layout.setOnClickListener {
          onClickListener(cargoListDto)
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
