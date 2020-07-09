package br.com.trivio.wms.ui.conference.cargo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.SearchInput
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.extensions.inflateToViewHolder
import br.com.trivio.wms.threatResult

class ConferenceItemsActivity : MyAppCompatActivity() {

  companion object {
    const val CARGO_CONFERENCE_ID: String = "CARGO_ID"
    const val GTIN_ID: String = "GTIN_ID"
    const val SEARCH_PRODUCT_GTIN: String = "PRODUCT_SEARCH_GTIN"
    const val REQUEST_CODE_SELECT_ITEM: Int = 1
    const val SUCCESS: Int = 1
  }

  private lateinit var cargoStatus: TaskStatus
  private var cargoItemsAdapter = CargoItemsAdapter(object : CargoItemsAdapter.OnClickCargoItem {
    override fun onClick(item: CargoConferenceItemDto) {
      if (!cargoStatus.isEnded)
        selectItemAndFinish(item)
      else
        showMessageError(R.string.counting_finished)
    }
  })

  private val viewModel: CargoConferenceViewModel by viewModels()
  private var cargoConferenceId: Long = 0
  private var search: String = ""

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_conference_items)
    setupToolbar()
    cargoConferenceId = intent.getLongExtra(CARGO_CONFERENCE_ID, 0)
    search = intent.getStringExtra(SEARCH_PRODUCT_GTIN)
    setupSearch()
    loadItems()
  }

  private fun setupSearch() {
    val inputSearch = findViewById<SearchInput>(R.id.search_input)
    inputSearch.addTextChangedListener { text: String ->
      search = text
      cargoItemsAdapter.items = viewModel.filter(search)
    }
    inputSearch.setText(search)
  }

  private fun loadItems() {
    val itemsRecyclerView = findViewById<RecyclerView>(R.id.cargo_items_recycler_view)
    itemsRecyclerView.layoutManager = LinearLayoutManager(this)
    itemsRecyclerView.adapter = cargoItemsAdapter
    viewModel.task.observe(this, Observer {
      threatResult(it, always = {
        endLoading()
      },
        onSuccess = { success ->
          cargoStatus = success.data.taskStatus
          cargoItemsAdapter.items = success.data.items
        })
    })
    loadCargoConference()
  }

  private fun loadCargoConference() {
    startLoading()
    viewModel.loadTask(cargoConferenceId)
  }

  private fun selectItemAndFinish(item: CargoConferenceItemDto) {
    val intent = Intent()
    intent.putExtra(GTIN_ID, item.gtin)
    setResult(SUCCESS, intent)
    finish()
  }
}

class CargoItemsAdapter(private val onClickCargoItem: OnClickCargoItem) :
  RecyclerView.Adapter<CargoItemsAdapter.CargoItemCountViewHolder>() {

  var items: List<CargoConferenceItemDto> = mutableListOf()
    set(value) {
      field = value
      notifyDataSetChanged()
    }

  class CargoItemCountViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private var productName = view.findViewById<TextView>(R.id.product_name)
    private var gtin = view.findViewById<TextView>(R.id.gtin_text)
    private var sku = view.findViewById<TextView>(R.id.sku_text)
    private var countedQtd = view.findViewById<TextView>(R.id.counted_qtd)
    fun bind(
      item: CargoConferenceItemDto,
      onClickCargoItem: OnClickCargoItem
    ) {
      var color = 0
      if (item.countedQuantity != null) {
        color = R.color.green_transparent
      }
      if (item.mismatchQuantity()) {
        color = R.color.red_transparent
      }
      if (color != 0)
        view.setBackgroundColor(view.context.getColor(color))
      productName.text = item.name
      gtin.text = coalesce(item.gtin, R.string.no_gtin)
      sku.text = coalesce(item.sku, R.string.no_sku)
      countedQtd.text = formatNumber(item.countedQuantity)
      view.setOnClickListener {
        onClickCargoItem.onClick(item)
      }
    }
  }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CargoItemCountViewHolder {
    return CargoItemCountViewHolder(parent.inflateToViewHolder(R.layout.item_conference_layout))
  }

  override fun getItemCount(): Int {
    return items.size
  }

  override fun onBindViewHolder(holder: CargoItemCountViewHolder, position: Int) {
    val item = items[position]
    holder.bind(item, onClickCargoItem)
  }

  interface OnClickCargoItem {
    fun onClick(item: CargoConferenceItemDto)
  }
}
