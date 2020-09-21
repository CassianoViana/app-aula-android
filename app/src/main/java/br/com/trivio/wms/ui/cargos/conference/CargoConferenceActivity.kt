package br.com.trivio.wms.ui.cargos.conference

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceDto.Companion.STATUS_COUNTING_ALL_COUNTED
import br.com.trivio.wms.data.dto.CargoConferenceDto.Companion.STATUS_COUNTING_NONE_COUNTED
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.extensions.Status.Companion.ERROR
import br.com.trivio.wms.extensions.Status.Companion.NOT_COMPLETED
import br.com.trivio.wms.extensions.Status.Companion.SUCCESS
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.cargos.FinishConferenceActivity
import br.com.trivio.wms.ui.tasks.TaskDetailsActivity
import br.com.trivio.wms.viewmodel.cargo.conference.CargoConferenceViewModel
import kotlinx.android.synthetic.main.activity_cargo_conference.*
import kotlinx.coroutines.launch
import java.math.BigDecimal


class CargoConferenceActivity : MyAppCompatActivity() {

  companion object {
    const val CARGO_TASK_ID: String = "CARGO_TASK_id"
    const val RESULT_TASK_ID: Int = 100
  }

  private var cargoConferenceTaskId: Long = 0
  private var cargoItemsAdapter = CargoItemsAdapter(object : CargoItemsAdapter.OnClickCargoItem {
    override fun onClick(item: CargoConferenceItemDto) {
      openCountItemDialog(item)
    }

    override fun onClickHistory(item: CargoConferenceItemDto) {
      openCountHistoryActivity(item)
    }

  })

  private val viewModel: CargoConferenceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargo_conference)
    cargoConferenceTaskId = intent.getLongExtra(CARGO_TASK_ID, 0)
    cargo_items_recycler_view.setAdapter(cargoItemsAdapter)
    observeViewModel()
    onBarcodeChangeSearchProduct()
    onRefreshLoadData()
    onClickFinish()
    onClickCameraOpenBarcodeReader()
    onReadBarcodeFillSearchInput()
    onClickReaderHideKeyboard()
    onClickHistoryOpenHistoryActivity()
    loadCargoConferenceTask()
  }

  /*
  private fun onClickFabScrollToTop() {
    fab_scroll_top_list.hide()
    app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
      override fun onOffsetChanged(p0: AppBarLayout?, p1: Int) {
        if (p1 == 0) {
          fab_scroll_top_list.hide()
        } else {
          fab_scroll_top_list.show()
        }
      }
    })
    fab_scroll_top_list.setOnClickListener {
      cargo_items_recycler_view.scrollTop();
      app_bar.setExpanded(true)
    }
  }*/

  private fun onClickHistoryOpenHistoryActivity() {
    btn_show_history.setOnClickListener {
      openCountHistoryActivity()
    }
  }

  private fun onClickReaderHideKeyboard() {
    barcode_reader.setOnClickListener {
      barcode_search_input.hideKeyboard()
    }
  }

  override fun onFinish() {
    barcode_reader.stop()
    val data = Intent()
    data.putExtra(CARGO_TASK_ID, this.cargoConferenceTaskId)
    setResult(RESULT_TASK_ID, data)
    super.onFinish()
  }

  override fun onResume() {
    super.onResume()
    resetReadingState()
    if (requestPermission(Manifest.permission.CAMERA)) {
      barcode_reader.start()
    }
  }

  override fun onPause() {
    super.onPause()
    barcode_search_input.setKeyboardVisible(false)
    barcode_reader.pause()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    barcode_reader.start()
  }

  private fun onReadBarcodeFillSearchInput() {
    barcode_reader.onRead = { it ->
      barcode_reader.pause()
      runOnUiThread {
        playAudio(this, R.raw.beep)
        barcode_search_input.applySearch(it)
      }
    }
  }

  private fun onClickCameraOpenBarcodeReader() {
    barcode_reader.setVisible(false)
    btn_open_camera.setOnClickListener {
      if (barcode_reader.toggleVisibility() == VISIBLE) {
        barcode_reader.start()
      } else {
        barcode_reader.stop()
      }
      barcode_search_input.hideKeyboard()
    }
  }

  private fun onClickFinish() {
    btn_finish_arrival.setOnClickListener {
      openEndConferenceActivity()
    }
  }

  private fun openEndConferenceActivity() {
    val intent = Intent(this, FinishConferenceActivity::class.java)
    intent.putExtra(FinishConferenceActivity.CARGO_TASK_ID, this.cargoConferenceTaskId)
    startActivityForResult(intent, FinishConferenceActivity.END_CONFERENCE_ACTIVITY)
  }

  private fun openCountHistoryActivity(item: CargoConferenceItemDto? = null) {
    val intent = Intent(this, ConferenceCountsActivity::class.java)
    intent.putExtra(ConferenceCountsActivity.CARGO_TASK_ID, cargoConferenceTaskId)
    item?.let {
      intent.putExtra(ConferenceCountsActivity.ITEM_CODE, item.gtin)
    }
    startActivityForResult(intent, ConferenceCountsActivity.END_COUNT_HISTORY_ACTIVITY)
  }

  private fun onRefreshLoadData() {
    cargo_items_recycler_view.setOnRefreshListener {
      loadCargoConferenceTask()
    }
  }

  private fun observeViewModel() {
    viewModel.cargoItem.observe(this, { result ->
      onResult(result,
        onSuccess = {
          showMessageSuccess(R.string.counted_success)
          loadCargoConferenceTask()
        },
        always = {
          cargo_items_recycler_view.stopRefresh()
        })
    })
    viewModel.task.observe(this, { result ->
      onResult(result,
        onSuccess = { success ->
          if (success.data.isPending()) {
            viewModel.startCounting(cargoConferenceTaskId)
            loadCargoConferenceTask()
          }
          updateUi(success.data)
        },
        onError = {
          showMessageError(R.string.error_while_loading_task)
        },
        always = {
          cargo_items_recycler_view.stopRefresh()
        }
      )
    })
    viewModel.finishStatus.observe(this, { result ->
      onResult(result, onSuccess = {
        setResult(TaskDetailsActivity.RESULT_TASK_CHANGED)
        clearTop()
      })
    })
    viewModel.items.observe(this, { result ->
      onResult(result, onSuccess = {
        updateCargoItems(it.data)
      })
    })
  }

  private fun onBarcodeChangeSearchProduct() {
    barcode_search_input.addOnTextChangeListener { searchInputValue ->
      viewModel.filterConferenceItems(searchInputValue)
    }
    barcode_search_input.addOnSearchListener { searchInputValue ->
      searchProductToCount(searchInputValue)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
      FinishConferenceActivity.END_CONFERENCE_ACTIVITY -> {
        data?.let {
          cargoConferenceTaskId =
            it.getLongExtra(FinishConferenceActivity.RESTARTING_TASK, cargoConferenceTaskId)
          loadCargoConferenceTask()
        }
      }
      ConferenceCountsActivity.END_COUNT_HISTORY_ACTIVITY -> {
        loadCargoConferenceTask()
      }
    }
  }

  private fun updateUi(cargoConferenceDto: CargoConferenceDto) {
    top_bar.setText(
      getString(
        R.string.cargo_conference_number,
        cargoConferenceDto.cargoReferenceCode
      )
    )
    updateCargoItems(cargoConferenceDto.items)
    updateUiLabelItemsCounted(cargoConferenceDto)
    if (cargoConferenceDto.taskStatus == TaskStatus.DONE) {
      updateUiDisableControls()
    } else {
      updateKeyboardStatusCargo(cargoConferenceDto)
    }
    endLoading()
  }

  private fun updateCargoItems(items: List<CargoConferenceItemDto>) {
    cargoItemsAdapter.items = items
    cargo_items_recycler_view.showEmptyLabel(items.isEmpty())
  }

  private fun updateUiDisableControls() {
    barcode_search_input.isEnabled = false
  }

  private fun updateKeyboardStatusCargo(dto: CargoConferenceDto) {
    if (dto.getStatusCounting() == STATUS_COUNTING_ALL_COUNTED) {
      showMessageSuccess(R.string.all_items_are_counted)
    }
  }

  private fun updateUiLabelItemsCounted(conferenceDto: CargoConferenceDto) {
    count_progress_bar.setProgress(conferenceDto.getPercentProgress())
    count_progress_bar.setStatus(
      when (conferenceDto.getStatusCounting()) {
        STATUS_COUNTING_NONE_COUNTED -> NOT_COMPLETED
        STATUS_COUNTING_ALL_COUNTED ->
          when {
            !conferenceDto.isValid() -> ERROR
            else -> SUCCESS
          }
        else -> NOT_COMPLETED
      }
    )
    count_progress_bar.setLabelTop(
      getString(
        R.string.progress_counting,
        conferenceDto.totalCountedItems,
        conferenceDto.quantityItems,
        (conferenceDto.restartCounter ?: 0).plus(1)
      )
    )
    count_progress_bar.setLabelBelow(
      getString(
        R.string.driver_and_truck_break_line,
        conferenceDto.driverName,
        conferenceDto.truckLabel
      )
    )
  }

  private fun loadCargoConferenceTask() {
    startLoading()
    viewModel.loadCargoConferenceTask(cargoConferenceTaskId)
  }

  override fun startLoading(loadingStringId: Int?) {
    cargo_items_recycler_view.setLoading(true)
  }

  override fun endLoading() {
    cargo_items_recycler_view.setLoading(false)
  }

  private fun searchProductToCount(search: String) {
    lifecycleScope.launch {
      onResult(viewModel.getCargoItem(search),
        onSuccess = {
          openCountItemDialog(it.data)
        },
        onNullResult = {
          showMessageError(getString(R.string.product_not_found_product_with_search, search))
          playErrorSound()
          resetReadingState()
        },
        onError = { resetReadingState() }
      )
    }
  }

  private fun openCountItemDialog(
    item: CargoConferenceItemDto,
    dialog: Dialog? = null
  ) {
    val totalQuantityCounted = item.countedQuantity?.toInt()
    val unitCode: String? = item.getUnitCode(getString(R.string.unit_code))
    val qtdInputNumber = createInputNumber(
      labelBeforeInput = getString(R.string.add_two_dots),
      labelAfterInput = unitCode,
      allowNegative = false,
    )
    prompt(
      dialog = dialog,
      firstTitle = getString(R.string.inform_qtds_asteristic),
      secondTitle = item.name,
      inputValue = totalQuantityCounted,
      hint = "0",
      inputView = qtdInputNumber,
      viewsBeforeInputFn = { _, views ->
        views.apply {
          add(
            createTextView(
              getString(R.string.no_damage_qtd),
              small = true
            )
          )
          createLayoutGtinCode(item).forEach { add(it) }
        }
      },
      viewsAfterInputFn = { dialog, views ->
        totalQuantityCounted?.let { qtd ->
          if (qtd > 0) {
            val labelWithCountedQtd =
              createTextView(
                getString(
                  R.string.already_conted_x_unities,
                  qtd,
                  unitCode
                )
              ).apply {
                this.setOnClickListener {
                  dialog.hide()
                  openCountHistoryActivity(item)
                }
              }
            views.add(labelWithCountedQtd)
          }
        }
      },
      negativeButtonText = if (item.damagedQuantity == null) {
        getString(R.string.inform_damage)
      } else {
        getString(R.string.number_damages, item.damagedQuantity?.toInt())
      },
      positiveAction = fun(dialog: Dialog, _) {
        qtdInputNumber.value?.let { quantity ->
          viewModel.countItem(item, quantity)
          item.count(quantity)
          cargoItemsAdapter.notifyDataSetChanged()
          resetReadingState()
          dialog.hide()
        }
      },
      negativeAction = { dialog ->
        openDamageDialog(item, onDamageReported = {
          openCountItemDialog(item, dialog)
        })
      },
      closeAction = {
        resetReadingState()
      }
    )
  }

  private fun resetReadingState() {
    barcode_search_input.reset()
    barcode_reader.start()
  }

  private fun openDamageDialog(
    item: CargoConferenceItemDto,
    onDamageReported: (item: CargoConferenceItemDto) -> Unit = {}
  ) {

    val unitCode: String? = item.getUnitCode(getString(R.string.unit_code))
    val damageCountInput =
      createInputNumber(
        labelBeforeInput = getString(R.string.add_two_dots),
        labelAfterInput = unitCode,
        allowNegative = false,
        backgroundDrawableResourceId = R.drawable.light_red_rounded
      )

    prompt(
      firstTitle = getString(R.string.how_many_items_are_damaged),
      secondTitle = item.name,
      positiveAction = { countDamageDialog, _: String ->
        damageCountInput.value?.let { damagedQtd ->
          if (damagedQtd.toInt() == 0) {
            showMessageError(R.string.the_quantity_cannot_be_zero)
          } else {
            if (damagedQtd.toInt() > 0) {
              prompt(
                firstTitle = getString(R.string.describe_damage),
                secondTitle = getString(R.string.describe_the_damage_template, item.name),
                positiveAction = { describeDamageDialog, value ->
                  viewModel.countItem(item, damagedQtd, value)
                  this.loadCargoConferenceTask()
                  item.addDamageQtd(damagedQtd)
                  describeDamageDialog.hide()
                  countDamageDialog.hide()
                  onDamageReported(item)
                },
                negativeButtonText = getString(R.string.inform_damage),
                /*viewsAfterInput = listOf(
                  createButton(getString(R.string.camera)) {
                    startCameraActivity()
                  }),*/
                inputType = InputType.TYPE_CLASS_TEXT,
                inputValue = "",
                hint = getString(R.string.damage_example),
                viewsBeforeInput = createLayoutGtinCode(item),
              )
            } else {
              viewModel.countItem(item, damagedQtd, getString(R.string.discount_damaged_quantity))
            }
          }
        }
      },
      negativeButtonText = getString(R.string.inform_damage),
      positiveButtonText = getString(R.string.damage_description),
      inputValue = BigDecimal.ZERO,
      inputView = damageCountInput,
      viewsBeforeInput = createLayoutGtinCode(item),

      viewsAfterInputFn = { _, views ->
        item.damagedQuantity?.let {
          it.toInt().let { intQtd ->
            if (intQtd > 0) {
              views.add(
                createTextView(
                  getString(
                    R.string.damages_already_counted,
                    intQtd,
                    unitCode
                  )
                )
              )
            }
          }
        }
      },
      drawableConfirmButtonResId = R.drawable.ic_baseline_arrow_forward_18,
    )
  }

  private fun createLayoutGtinCode(item: CargoConferenceItemDto): List<View> {
    return listOf(inflate<View>(R.layout.product_codes).apply {
      this.findViewById<TextView>(R.id.sku_text).text = item.sku
      this.findViewById<TextView>(R.id.gtin_text).text = coalesce(item.gtin, R.string.no_gtin)
    })
  }

  class CargoItemsAdapter(private val onClickCargoItem: OnClickCargoItem) :
    RecyclerView.Adapter<CargoItemsAdapter.CargoItemCountViewHolder>() {

    var items: List<CargoConferenceItemDto> = mutableListOf()
      set(value) {
        field = value
        notifyDataSetChanged()
      }

    class CargoItemCountViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
      private var productNameTextView = view.findViewById<TextView>(R.id.product_name)
      private var gtinTextView = view.findViewById<TextView>(R.id.gtin_text)
      private var skuTextView = view.findViewById<TextView>(R.id.sku_text)
      private var storageUnitTextView = view.findViewById<TextView>(R.id.storage_unit_text)
      private var countedQtdTextView = view.findViewById<TextView>(R.id.counted_qtd)
      private var damagedQtdTextView = view.findViewById<TextView>(R.id.damaged_qtd)
      private var countingOkTextView = view.findViewById<TextView>(R.id.counting_status_ok)
      private var countingNotOkTextView =
        view.findViewById<TextView>(R.id.counting_status_divergent)
      private var countingPendingTextView =
        view.findViewById<TextView>(R.id.counting_status_pending)
      private var countHistoryTextView =
        view.findViewById<Button>(R.id.count_history_btn)
      private var itemToClick = view.findViewById<View>(R.id.item_to_click)

      fun bind(
        item: CargoConferenceItemDto,
        onClickCargoItem: OnClickCargoItem
      ) {
        countingNotOkTextView.setVisible(item.isCountedWithDivergences())
        countingOkTextView.setVisible(item.isCountedCorrectly())
        countingPendingTextView.setVisible(!item.isCounted())
        countedQtdTextView.setVisible(item.countedQuantity != null)
        damagedQtdTextView.setVisible(item.hasDamagedQtd())
        storageUnitTextView.setVisible(item.storageUnit != null)
        countHistoryTextView.setVisible(item.isCounted())
        countHistoryTextView.setOnClickListener {
          onClickCargoItem.onClickHistory(item)
        }
        countHistoryTextView.text = view.context.getString(R.string.historic_qtd, item.counts.size)

        item.damagedQuantity?.let { damagedQtd ->
          damagedQtdTextView.text =
            view.context.getString(R.string.damaged_items, damagedQtd.toInt())
        }

        item.storageUnit?.let {
          storageUnitTextView.text = it.code
        }

        gtinTextView.text = coalesce(item.gtin, R.string.no_gtin)
        productNameTextView.text = item.name
        skuTextView.text = coalesce(item.sku, R.string.no_sku)
        countedQtdTextView.text = formatNumber(item.countedQuantity)
        itemToClick.setOnClickListener {
          onClickCargoItem.onClick(item)
        }
        /*view.setOnLongClickListener {
          onClickCargoItem.onClickHistory(item)
          false
        }*/
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
      fun onClickHistory(item: CargoConferenceItemDto)
    }
  }
}
