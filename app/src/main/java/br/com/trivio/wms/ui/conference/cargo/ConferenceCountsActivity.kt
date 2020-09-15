package br.com.trivio.wms.ui.conference.cargo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.Badge
import br.com.trivio.wms.data.dto.ConferenceCountDto
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.onResult
import kotlinx.android.synthetic.main.activity_conference_counts.*

class ConferenceCountsActivity : MyAppCompatActivity() {

  companion object {
    const val END_COUNT_HISTORY_ACTIVITY: Int = 100
    const val CARGO_TASK_ID: String = "cargo_task_id"
    const val ITEM_CODE: String = "item_code"
  }

  private var conferenceTaskId: Long = 0
  private var itemCodeSearch: String = ""
  private val viewModel: CargoConferenceViewModel by viewModels()
  private val adapter = ConferenceCountsAdapter(
    onClickToDeleteListener = { conferenceCountDto ->
      AlertDialog.Builder(this)
        .setTitle(R.string.delete_count)
        .setPositiveButton(
          R.string.yes
        ) { _, _ ->
          viewModel.undoCount(conferenceCountDto) {
            onResult(
              it,
              onSuccess = {
                showMessageSuccess(R.string.the_count_was_undone)
              },
              onError = {
                showMessageWarning(R.string.the_count_item_already_modified)
              },
              always = {
                loadCountHistory()
              },
              showErrorMessage = false
            );
          }
        }
        .setNegativeButton(R.string.no, null)
        .show();

    }
  )

  override fun onFinish() {
    setResult(END_COUNT_HISTORY_ACTIVITY)
    finish()
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_conference_counts)
    this.setupToolbar()

    conferenceTaskId = intent.getLongExtra(CARGO_TASK_ID, 0)
    itemCodeSearch = intent.getStringExtra(ITEM_CODE) ?: ""
    count_list.setAdapter(adapter)

    observeViewModel()
    onRefreshLoadData()
    onSearchFilterProducts()
    loadCountHistory()
  }

  private fun onSearchFilterProducts() {
    input_search_product.setText(itemCodeSearch)
    input_search_product.addOnTextChangeListener {
      itemCodeSearch = it
      filterItemsBySearch()
    }
  }

  private fun filterItemsBySearch() {
    setHistoryData(viewModel.filterCountHistory(itemCodeSearch))
  }

  private fun onRefreshLoadData() {
    count_list.setOnRefreshListener {
      loadCountHistory()
    }
  }

  private fun observeViewModel() {
    viewModel.countsHistoryList.observe(this, {
      onResult(it,
        onSuccess = { successResult ->
          setHistoryData(successResult.data)
          if (itemCodeSearch.isNotEmpty()) {
            filterItemsBySearch()
          }
        },
        always = {
          count_list.stopRefresh()
        }
      )
    })
  }

  private fun setHistoryData(data: List<ConferenceCountDto>) {
    adapter.items = data
    count_list.showEmptyLabel(data.isEmpty())
  }

  fun loadCountHistory() {
    viewModel.loadCountHistory(conferenceTaskId)
  }

  class ConferenceCountsAdapter(var onClickToDeleteListener: (count: ConferenceCountDto) -> Unit) :
    RecyclerView.Adapter<ConferenceCountItemViewHolder>() {

    var items: List<ConferenceCountDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    override fun onCreateViewHolder(
      parent: ViewGroup,
      viewType: Int
    ): ConferenceCountItemViewHolder {
      return ConferenceCountItemViewHolder(parent.inflateToViewHolder(R.layout.item_conference_count_layout))
    }

    override fun onBindViewHolder(holder: ConferenceCountItemViewHolder, position: Int) {
      holder.bind(items[position], onClickToDeleteListener)
    }

    override fun getItemCount(): Int {
      return items.size
    }

  }

  class ConferenceCountItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val trashBtn = view.findViewById<Button>(R.id.btn_trash_delete_count)
    private val dateTxtView = view.findViewById<TextView>(R.id.count_date_text_view)
    private val itemCodeTxtView = view.findViewById<TextView>(R.id.item_code_text_view)
    private val itemProductTxtView = view.findViewById<TextView>(R.id.count_product_name_text_view)
    private val itemGtinTxtView = view.findViewById<TextView>(R.id.item_gtin_text_view)
    private val userNameTxtView = view.findViewById<TextView>(R.id.count_user_name_text_view)
    private val countItemTypeBadge = view.findViewById<Badge>(R.id.count_item_type_badge)
    private val descriptionTxtView = view.findViewById<TextView>(R.id.description_text_view)
    private val countItemQtdTxtView = view.findViewById<TextView>(R.id.count_item_qtd_text_view)
    private val storageUnitTxtView = view.findViewById<TextView>(R.id.storage_unit_text_view)

    fun bind(item: ConferenceCountDto, onClickDelete: (item: ConferenceCountDto) -> Unit) {
      trashBtn.setOnClickListener {
        onClickDelete(item)
      }
      dateTxtView.text = item.created?.formatTo("dd/MM/yyyy HH:mm")
      itemProductTxtView.text = item.product
      itemCodeTxtView.text = item.sku
      itemGtinTxtView.text = item.gtin
      userNameTxtView.text = item.username
      countItemTypeBadge.text = item.countType?.name
      countItemTypeBadge.backgroundColor = item.countType?.color
      countItemQtdTxtView.text = item.count?.toInt().toString()
      storageUnitTxtView.text = item.storageUnitCode
      descriptionTxtView.setVisible(item.description != null)
      descriptionTxtView.text = item.description
    }
  }
}
