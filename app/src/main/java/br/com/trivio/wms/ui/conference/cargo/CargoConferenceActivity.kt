package br.com.trivio.wms.ui.conference.cargo

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.call
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceDto.Companion.STATUS_COUNTING_ALL_COUNTED
import br.com.trivio.wms.data.dto.CargoConferenceDto.Companion.STATUS_COUNTING_NONE_COUNTED
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.dto.DamageDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.extensions.Status.Companion.ERROR
import br.com.trivio.wms.extensions.Status.Companion.NOT_COMPLETED
import br.com.trivio.wms.extensions.Status.Companion.SUCCESS
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.cargos.EndConferenceActivity
import br.com.trivio.wms.ui.tasks.TaskDetailsActivity
import kotlinx.android.synthetic.main.activity_cargo_conference.*
import kotlinx.coroutines.launch
import java.math.BigDecimal


class CargoConferenceActivity : MyAppCompatActivity() {

  companion object {
    const val CARGO_TASK_ID: String = "CARGO_TASK_id"
  }

  private var cargoConferenceTaskId: Long = 0
  private var cargoItemsAdapter = CargoItemsAdapter(object : CargoItemsAdapter.OnClickCargoItem {
    override fun onClick(item: CargoConferenceItemDto) {
      requestQuantity(item.gtin)
    }
  })

  private val viewModel: CargoConferenceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargo_conference)
    setupToolbar()

    cargoConferenceTaskId = intent.getLongExtra(CARGO_TASK_ID, 0)
    cargo_items_recycler_view.setAdapter(cargoItemsAdapter)
    progress_bar.setText(getString(R.string.loading_string))

    observeViewModel()
    onBarcodeChangeSearchProduct()
    onRefreshLoadData()
    onClickFinish()
    loadData()
  }

  override fun onPause() {
    super.onPause()
    barcode.setKeyboardVisible(false)
  }

  private fun onClickFinish() {
    progress_bar.setOnClickListener {
      val intent = Intent(this, EndConferenceActivity::class.java)
      intent.putExtra(EndConferenceActivity.CARGO_TASK_ID, this.cargoConferenceTaskId)
      startActivityForResult(intent, EndConferenceActivity.END_CONFERENCE_ACTIVITY)
    }
  }

  private fun onRefreshLoadData() {
    cargo_items_recycler_view.setOnRefreshListener {
      loadData()
    }
  }

  private fun observeViewModel() {
    viewModel.cargoItem.observe(this, Observer {
      onResult(it,
        onSuccess = {
          showMessageSuccess(R.string.counted_success)
          loadCargoConferenceTask()
        },
        always = {
          cargo_items_recycler_view.stopRefresh()
        })
    })
    viewModel.damageRegistration.observe(this, Observer {
      onResult(it,
        onSuccess = { showMessageSuccess(R.string.damage_was_registered) },
        onError = { showMessageError(R.string.error_while_register_damage) }
      )
    })
    viewModel.task.observe(this, Observer {
      onResult(it,
        onSuccess = { result ->
          updateUi(result.data)
        },
        onError = {
          showMessageError(R.string.error_while_loading_task)
        },
        always = {
          cargo_items_recycler_view.stopRefresh()
        }
      )
    })
    viewModel.finishStatus.observe(this, Observer {
      onResult(it, onSuccess = {
        setResult(TaskDetailsActivity.RESULT_TASK_CHANGED)
        finish()
      })
    })
  }

  private fun onBarcodeChangeSearchProduct() {
    barcode.addTextChangedListener {
      val gtin = it
      cargoItemsAdapter.items = viewModel.filter(gtin)
      if (gtin.length >= 5) {
        requestQuantity(gtin)
      }
    }
  }

  private fun loadData() {
    lifecycleScope.launchWhenCreated {
      loadCargoConferenceTask()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data);
    when (resultCode) {
      EndConferenceActivity.END_CONFERENCE_ACTIVITY -> {
        data?.let {
          cargoConferenceTaskId =
            it.getLongExtra(EndConferenceActivity.RESTARTING_TASK, cargoConferenceTaskId)
          loadCargoConferenceTask()
        }
      }
    }
  }

  private fun updateUi(cargoConferenceDto: CargoConferenceDto) {
    top_bar.setText(getString(R.string.cargo_conference_number, cargoConferenceDto.taskId))
    cargoItemsAdapter.items = cargoConferenceDto.items
    updateUiLabelItemsCounted(cargoConferenceDto)
    if (cargoConferenceDto.taskStatus == TaskStatus.DONE) {
      updateUiDisableControls()
    } else {
      updateBtnFinishTask(cargoConferenceDto)
      updateKeyboardStatusCargo(cargoConferenceDto)
    }
    endLoading()
  }

  private fun updateUiDisableControls() {
    barcode.isEnabled = false
  }

  private fun updateKeyboardStatusCargo(dto: CargoConferenceDto) {
    val allCounted = dto.getStatusCounting() != STATUS_COUNTING_ALL_COUNTED
    if (allCounted) {

    }
  }

  private fun updateUiLabelItemsCounted(data: CargoConferenceDto) {
    val totalItems = data.quantityItems
    val totalCountedItems = data.getTotalCountedItems()
    val totalDivergentItems = data.getTotalDivergentItems()
    val totalCorrectCounted = data.getTotalCorrectCountedItems()
    val statusCounting = data.getStatusCounting()

    val labelStatusCounting =
      getString(
        R.string.truck_label__one_from_max_counted,
        data.truckLabel,
        totalCountedItems,
        data.items.size
      )

    val progressStatus =
      when (statusCounting) {
        STATUS_COUNTING_NONE_COUNTED -> NOT_COMPLETED
        STATUS_COUNTING_ALL_COUNTED ->
          when {
            totalDivergentItems > 0 -> ERROR
            else -> SUCCESS
          }
        else -> NOT_COMPLETED
      }

    val colors = data.items.map {
      when {
        it.correctCounted() -> SUCCESS.color
        it.mismatchQuantity() -> ERROR.color
        else -> NOT_COMPLETED.color
      }
    }

    progress_bar.setStatus(progressStatus)
    progress_bar.setText(labelStatusCounting)
    progress_bar.setColors(colors.map { getColor(it) })
  }

  private fun updateBtnFinishTask(data: CargoConferenceDto) {
    val statusCounting = data.getStatusCounting()
    /*btn_finish_conference.setVisible(statusCounting == STATUS_COUNTING_ALL_COUNTED)*/
  }

  private fun loadCargoConferenceTask() {
    startLoading()
    viewModel.loadCargoConferenceTask(cargoConferenceTaskId)
  }

  private fun requestQuantity(gtin: String) {
    lifecycleScope.launch {
      call(
        { viewModel.getCargoItem(gtin) },
        onSuccess = { promptQtd(it.data) }
      )
    }
  }

  private fun promptQtd(item: CargoConferenceItemDto) {
    val reportDamageButtons = createButton(getString(R.string.report_damage))
    val buttonsList = listOf(reportDamageButtons)
    prompt(
      firstTitle = item.name,
      secondTitle = getString(R.string.how_many_items_were_conted),
      inputValue = item.countedQuantity,
      closeAction = {
        resetBarcode()
        showMessageInfo(R.string.cancelled_operation)
      },
      viewsToAdd = buttonsList
    ) { dialog: Dialog, value: String ->
      validateCountQuantity(value) { quantity ->
        viewModel.countItem(item, quantity)
        resetBarcode()
        dialog.hide()
      }
    }
    reportDamageButtons.setOnClickListener {
      reportDamage(item)
    }
  }

  private fun resetBarcode() {
    barcode.reset()
  }

  private fun reportDamage(item: CargoConferenceItemDto) {

    var dialogDescriptionDialog: Dialog? = null
    var dialogQuantityDialog: Dialog? = null

    val damageDto = item.damageDto ?: DamageDto()

    // Page 2
    dialogDescriptionDialog = prompt(
      firstTitle = getString(R.string.describe_damage),
      secondTitle = getString(R.string.describe_the_damage_template, item.name),
      keepClosedOnCreate = true,
      inputType = InputType.TYPE_CLASS_TEXT,
      inputValue = damageDto.description,
      hint = getString(R.string.damage_example),
      positiveAction = { _, value ->
        damageDto.description = value
        damageDto.cargoItemId = item.cargoItemId
        viewModel.registerDamage(damageDto)
        dialogQuantityDialog?.hide()
        dialogDescriptionDialog?.hide()
      }
    )

    // Page 1
    dialogQuantityDialog = prompt(
      firstTitle = getString(R.string.count_the_damage),
      secondTitle = getString(R.string.how_many_items_were_damaged_template, item.name),
      inputValue = damageDto.quantity
    ) { _, value: String ->
      validateCountQuantity(value) {
        damageDto.quantity = it
        dialogDescriptionDialog.show()
      }
    }
  }

  private fun validateCountQuantity(value: String, onSuccess: (value: BigDecimal) -> Any) {
    try {
      val bigDecimal = BigDecimal(value)
      onSuccess(bigDecimal)
    } catch (e: Exception) {
      showMessageError(R.string.please_check_the_quantity)
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
      private var icon = view.findViewById<ImageView>(R.id.icon)
      private var gtin = view.findViewById<TextView>(R.id.gtin_text)
      private var sku = view.findViewById<TextView>(R.id.sku_text)
      private var countedQtd = view.findViewById<TextView>(R.id.counted_qtd)
      fun bind(
        item: CargoConferenceItemDto,
        onClickCargoItem: OnClickCargoItem
      ) {
        val status = when {
          item.correctCounted() -> SUCCESS
          item.mismatchQuantity() -> ERROR
          else -> NOT_COMPLETED
        }
        icon.setVisible(status.icon != null)
        when (status) {
          NOT_COMPLETED -> {
            icon.clearColorFilter()
          }
          else -> {
            icon.setImageResource(status.icon!!)
            icon.setColorFilter(view.context.getColor(status.color))
          }
        }
        gtin.text = coalesce(item.gtin, R.string.no_gtin)
        productName.text = item.name
        sku.text = coalesce(item.sku, R.string.no_sku)
        countedQtd.text = formatNumber(item.countedQuantity)
        view.setOnClickListener {
          onClickCargoItem.onClick(item)
        }
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CargoItemCountViewHolder {
      val items = parent.inflateToViewHolder(R.layout.item_conference_layout)
      return CargoItemCountViewHolder(items)
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
}
