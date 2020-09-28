package br.com.trivio.wms.ui.picking

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.Badge
import br.com.trivio.wms.components.custom.BarcodeReader
import br.com.trivio.wms.components.custom.Message
import br.com.trivio.wms.components.custom.RefreshableList
import br.com.trivio.wms.data.dto.PickStockPositionDto
import br.com.trivio.wms.data.dto.PickingItemDto
import br.com.trivio.wms.data.dto.PickingTaskDto
import br.com.trivio.wms.data.dto.PickingTaskDto.Companion.STATUS_PICKING_ALL_PICKED
import br.com.trivio.wms.data.dto.PickingTaskDto.Companion.STATUS_PICKING_NONE_PICKED
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.extensions.*
import br.com.trivio.wms.extensions.Status.Companion.NOT_COMPLETED
import br.com.trivio.wms.extensions.Status.Companion.SUCCESS
import br.com.trivio.wms.onResult
import br.com.trivio.wms.ui.equipments.AddEquipmentsListActivity
import br.com.trivio.wms.ui.equipments.ReleaseEquipmentsListActivity
import br.com.trivio.wms.viewmodel.picking.PickingViewModel
import kotlinx.android.synthetic.main.activity_picking.*
import kotlinx.coroutines.launch


class PickingActivity : MyAppCompatActivity() {

  companion object {
    const val PICKING_TASK_ID: String = "PICKING_TASK_id"
    const val RESULT_TASK_ID: Int = 100
  }

  private var pickingTaskId: Long = 0
  private var pickingItemsAdapter = PickingItemsAdapter(object : PickingItemsAdapter.OnClickItem {
    override fun onClick(item: PickingItemDto) {
      //openPickItemDialog(item)
      openPositionDialog(item)
    }

    /*override fun onClickHistory(item: PickingItemDto) {
      openEquipmentsListActivity(item)
    }*/

  })

  private val viewModel: PickingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_picking)
    pickingTaskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    picking_items_recycler_view.setAdapter(pickingItemsAdapter)
    observeViewModel()
    onBarcodeChangeSearchProduct()
    onRefreshLoadData()
    onClickFinishGoToReleaseEquipmentsActivity()
    onClickCameraOpenBarcodeReader()
    onReadBarcodeFillSearchInput()
    onClickReaderHideKeyboard()
    onClickEquipmentsIconOpenEquipmentsActivity()
    loadPickingTask()
  }

  private fun createBarcodeReader(hint: String): BarcodeReader {
    return BarcodeReader(this).apply {
      setHint(hint)
      setInputVisible()
      setToggleable()
      startRead()
      setMarginVertical(margin = 20, height = 400)
    }
  }

  private fun openPositionDialog(item: PickingItemDto, dialogToReuse: Dialog? = null) {
    val onValidPositionCode: (Dialog) -> Unit = { positionDialog ->
      openProductDialog(item,
        onCloseProductDialogListener = {
          viewModel.findNextItem(item) { it ->
            onResult(it,
              onSuccess = {
                openPositionDialog(it.data, positionDialog)
              })
          }
        })
    }
    val barcodeReader = createBarcodeReader("R00.B00.A00.L00")
    val positionInput = barcodeReader.getInput()
    prompt(
      dialog = dialogToReuse,
      firstTitle = getString(
        R.string.position_x_of_total,
        item.order,
        item.totalItemsTask
      ),
      secondTitle = getString(
        R.string.confirm_position,
        item.position
      ),
      hideDefaultInputView = true,
      inputViewFn = { dialog ->
        barcodeReader.apply {
          setOnReadListener { position ->
            validatePosition(item, position,
              onValid = { onValidPositionCode(dialog) },
              onInvalid = {
                delay {
                  startRead()
                }
              })
          }
        }
      },
      positiveAction = { dialog, position ->
        validatePosition(item, position,
          onValid = { onValidPositionCode(dialog) },
          onInvalid = {
            positionInput.selectAll()
          })
      }
    )
  }

  private fun openProductDialog(
    item: PickingItemDto,
    onCloseProductDialogListener: () -> Unit = {}
  ) {
    val barcodeReader = createBarcodeReader(hint = "Código de barras")
    val productInput = barcodeReader.getInput()
    val onValidProductCode: (Dialog) -> Any = { productDialog ->
      openPickItemDialog(item,
        onFinishPickItem = {
          productDialog.hide()
          onCloseProductDialogListener()
        })
    }
    prompt(
      firstTitle = getString(R.string.confirm_product),
      hideDefaultInputView = true,
      viewsBeforeInputFn = { _, views ->
        views.add(createPickItemLayout(item))
      },
      inputViewFn = { dialog ->
        barcodeReader.apply {
          setOnReadListener { productCode ->
            validateProductCode(item, productCode,
              onValid = { onValidProductCode(dialog) },
              onInvalid = {
                delay {
                  startRead()
                }
              })
          }
        }
      },
      positiveAction = { dialog, productCode ->
        validateProductCode(item, productCode,
          onValid = { onValidProductCode(dialog) },
          onInvalid = {
            productInput.selectAll()
          })
      }
    )
  }

  private fun validatePosition(
    item: PickingItemDto,
    position: String,
    onValid: () -> Unit = {},
    onInvalid: () -> Unit = {}
  ) {
    viewModel.validatePosition(item, position) {
      onResult(it,
        showErrorMessage = false,
        onSuccess = {
          playBeep()
          onValid()
        },
        onError = {
          playErrorSound()
          showMessageError(R.string.position_no_correspondent)
          onInvalid()
        })
    }
  }

  private fun validateProductCode(
    item: PickingItemDto,
    code: String,
    onValid: () -> Unit = {},
    onInvalid: () -> Unit = {}
  ) {
    viewModel.validateProduct(item, code) {
      onResult(it,
        showErrorMessage = false,
        onSuccess = {
          playBeep()
          onValid()
        },
        onError = {
          playErrorSound()
          showMessageError(R.string.code_no_correspondent)
          onInvalid()
        })
    }
  }

  private fun onClickEquipmentsIconOpenEquipmentsActivity() {
    btn_equipments.setOnClickListener {
      openEquipmentsListActivity()
    }
  }

  @Deprecated(message = "O campo de busca direta será removido")
  private fun onClickReaderHideKeyboard() {
    barcode_reader.setOnClickListener {
      barcode_search_input.hideKeyboard()
    }
  }

  override fun onFinish() {
    barcode_reader.stopReading()
    val data = Intent()
    data.putExtra(PICKING_TASK_ID, this.pickingTaskId)
    setResult(RESULT_TASK_ID, data)
    super.onFinish()
  }

  override fun onResume() {
    super.onResume()
    resetReadingState()
    if (requestPermission(Manifest.permission.CAMERA)) {
      barcode_reader.startRead()
    }
  }

  override fun onPause() {
    super.onPause()
    barcode_search_input.setKeyboardVisible(false)
    barcode_reader.pauseReading()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    barcode_reader.startRead()
  }

  @Deprecated(message = "O campo de busca direta será removido")
  private fun onReadBarcodeFillSearchInput() {
    barcode_reader.onRead = { it ->
      barcode_reader.pauseReading()
      runOnUiThread {
        playBeep()
        barcode_search_input.applySearch(it)
      }
    }
  }

  @Deprecated(message = "O campo de busca direta será removido")
  private fun onClickCameraOpenBarcodeReader() {
    barcode_reader.setVisible(false)
    btn_open_camera.setOnClickListener {
      if (barcode_reader.toggleVisibility() == VISIBLE) {
        barcode_reader.startRead()
      } else {
        barcode_reader.stopReading()
      }
      barcode_search_input.hideKeyboard()
    }
  }

  private fun onClickFinishGoToReleaseEquipmentsActivity() {
    btn_finish_picking.setOnClickListener {
      openReleaseEquipmentsActivity()
    }
  }

  private fun openReleaseEquipmentsActivity() {
    val intent = Intent(this, ReleaseEquipmentsListActivity::class.java)
    intent.putExtra(ReleaseEquipmentsListActivity.PICKING_TASK_ID, this.pickingTaskId)
    startActivityForResult(intent, ReleaseEquipmentsListActivity.END_ACTIVITY)
  }

  private fun openEquipmentsListActivity(item: PickingItemDto? = null) {
    val intent = Intent(this, AddEquipmentsListActivity::class.java)
    intent.putExtra(AddEquipmentsListActivity.PICKING_TASK_ID, pickingTaskId)
    startActivityForResult(intent, AddEquipmentsListActivity.END_ACTIVITY)
  }

  private fun onRefreshLoadData() {
    picking_items_recycler_view.setOnRefreshListener {
      loadPickingTask()
    }
  }

  private fun observeViewModel() {
    viewModel.pickingItem.observe(this, { result ->
      onResult(result,
        onSuccess = {
          showMessageSuccess(R.string.picking_success)
          loadPickingTask()
        },
        always = {
          picking_items_recycler_view.stopRefresh()
        })
    })
    viewModel.task.observe(this, { result ->
      onResult(result,
        onSuccess = { success ->
          if (success.data.isPending()) {
            viewModel.startPicking(pickingTaskId)
            loadPickingTask()
          }
          updateUi(success.data)
        },
        onError = {
          showMessageError(R.string.error_while_loading_task)
        },
        always = {
          picking_items_recycler_view.stopRefresh()
        }
      )
    })

    /*viewModel.finishStatus.observe(this, { result ->
      onResult(result, onSuccess = {
        setResult(TaskDetailsActivity.RESULT_TASK_CHANGED)
        clearTop()
      })
    })*/

    viewModel.items.observe(this, { result ->
      onResult(result, onSuccess = {
        updateCargoItems(it.data)
      })
    })
  }

  @Deprecated(message = "O campo de busca direta será removido")
  private fun onBarcodeChangeSearchProduct() {
    layout_search_input.setVisible(false)
    barcode_search_input.addOnTextChangeListener { searchInputValue ->
      viewModel.filterPickingItems(searchInputValue)
    }
    barcode_search_input.addOnSearchListener { searchInputValue ->
      searchProductToCount(searchInputValue)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    when (resultCode) {
      /*EndConferenceActivity.END_CONFERENCE_ACTIVITY -> {
        data?.let {
          pickingTaskId =
            it.getLongExtra(EndConferenceActivity.RESTARTING_TASK, pickingTaskId)
          loadPickingTask()
        }
      }
      ConferenceCountsActivity.END_COUNT_HISTORY_ACTIVITY -> {
        loadPickingTask()
      }*/
    }
  }

  private fun updateUi(cargoConferenceDto: PickingTaskDto) {
    updateCargoItems(cargoConferenceDto.items)
    updateUiLabelItemsCounted(cargoConferenceDto)
    if (cargoConferenceDto.taskStatus == TaskStatus.DONE) {
      updateUiDisableControls()
    } else {
      updateStatusCargo(cargoConferenceDto)
    }
    endLoading()
  }

  private fun updateCargoItems(items: List<PickingItemDto>) {
    pickingItemsAdapter.items = items
    picking_items_recycler_view.showEmptyLabel(items.isEmpty())
  }

  private fun updateUiDisableControls() {
    barcode_search_input.isEnabled = false
  }

  private fun updateStatusCargo(dto: PickingTaskDto) {
    if (dto.getStatusPicking() == STATUS_PICKING_ALL_PICKED) {
      showMessageSuccess(R.string.all_items_are_counted)
    }
  }

  private fun updateUiLabelItemsCounted(pickingTaskDto: PickingTaskDto) {
    count_progress_bar.setProgress(pickingTaskDto.progress)
    count_progress_bar.setStatus(
      when (pickingTaskDto.getStatusPicking()) {
        STATUS_PICKING_NONE_PICKED -> NOT_COMPLETED
        STATUS_PICKING_ALL_PICKED -> SUCCESS
        else -> NOT_COMPLETED
      }
    )
    count_progress_bar.setLabelTop(
      getString(
        R.string.progress_picking,
        pickingTaskDto.quantityPickedItems,
        pickingTaskDto.quantityItems,
      )
    )
    count_progress_bar.setLabelBelow(
      getString(
        R.string.ped_and_number,
        pickingTaskDto.orderNumber
      )
    )
  }

  private fun loadPickingTask() {
    startLoading()
    viewModel.loadPickingTask(pickingTaskId)
  }

  override fun startLoading(loadingStringId: Int?) {
    picking_items_recycler_view.setLoading(true)
  }

  override fun endLoading() {
    picking_items_recycler_view.setLoading(false)
  }

  private fun searchProductToCount(search: String) {
    lifecycleScope.launch {
      onResult(viewModel.getPickingItem(search),
        onSuccess = {
          openAlertProductNotFoundInPickingTask(it.data)
          openPickItemDialog(it.data)
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

  private fun openAlertProductNotFoundInPickingTask(item: PickingItemDto) {
    prompt(
      firstTitle = getString(R.string.warning),
      viewsBeforeInputFn = { _, views ->
        views.add(createPickItemLayout(item))
        views.add(Message(this).apply {
          setType(Message.TYPE_WARNING)
          message = getString(R.string.product_not_found_in_picking_task)
          setMarginVertical()
        })
      },
      positiveAction = { dialog, _: String ->
        dialog.hide()
      },
      positiveButtonText = getString(R.string.understod),
    )
  }

  private fun openPickItemDialog(
    item: PickingItemDto,
    onFinishPickItem: (item: PickingItemDto) -> Any? = {}
  ) {
    val unitCode: String? = item.getUnitCode(getString(R.string.unit_code))
    val qtdInputNumber = createInputNumber(
      labelBeforeInput = getString(R.string.separadas),
      labelAfterInput = unitCode,
      allowNegative = false,
    )
    prompt(
      firstTitle = getString(R.string.pick),
      hint = "0",
      inputView = qtdInputNumber,
      viewsBeforeInputFn = { _, views ->
        views.apply {
          add(createPickItemLayout(item))
        }
      },
      viewsAfterInputFn = { _, views ->
        views.apply {
          add(
            createTextView(
              value = getString(
                R.string.qtd_aready_separed,
                item.pickedQuantity?.toInt(),
                item.getUnitCode()
              ),
            )
          )
        }
      },
      negativeButtonText = getString(R.string.not_found),
      positiveButtonText = getString(R.string.next),
      drawableConfirmButtonResId = R.drawable.ic_baseline_arrow_forward_18,
      positiveAction = fun(dialog: Dialog, _) {
        qtdInputNumber.value?.let { quantity ->
          viewModel.pickItem(item, quantity) { it ->
            onResult(it, onSuccess = {
              pickingItemsAdapter.notifyDataSetChanged()
              onFinishPickItem(item)
              delay {
                dialog.hide()
              }
            })
          }
        }
      },
      negativeAction = { dialog ->
        openStockSearchDialog(item)
      },
    )
  }

  @Deprecated("O campo será removido")
  private fun resetReadingState() {
    //barcode_search_input.reset()
    //barcode_reader.startRead()
  }

  private fun openStockSearchDialog(
    item: PickingItemDto,
    //onPositionSelected: (item: PickingItemDto) -> Unit = {}
  ) {

    prompt(
      firstTitle = getString(R.string.product_stock),
      viewsAfterInputFn = { _, views ->
        views.add(createStockList(item).apply {
          setWeight(1)
        })
        if (item.hasRequestedPickingReposition) {
          views.add(Message(this).apply {
            setType(Message.TYPE_WARNING)
            message = getString(R.string.already_exists_reposition_request)
            setMarginVertical()
          })
        }
      },
      positiveAction = { dialog, _: String ->
      },
      positiveButtonText = getString(
        if (item.hasRequestedPickingReposition) R.string.cancel_request_picking_reposition
        else R.string.request_picking_reposition
      ),
      viewsBeforeInput = listOf(createPickItemLayout(item)),
    )
  }

  private fun createStockList(item: PickingItemDto): RefreshableList {
    return inflate<RefreshableList>(R.layout.pick_item_stock_positions_list).apply {

      this.setOnRefreshListener {
        delay(200) {
          this.stopRefresh()
        }
      }

      class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var positionTextView = view.findViewById<TextView>(R.id.position_name_text_view)
        private var positionTypeTextView =
          view.findViewById<TextView>(R.id.position_type_text_view)
        private var positionQtdItemsTextView =
          view.findViewById<TextView>(R.id.position_qtd_itens_text_view)
        private var positionUnitTextView =
          view.findViewById<TextView>(R.id.position_unit_text_view)

        fun bind(dto: PickStockPositionDto) {
          positionTextView.text = dto.name
          positionTypeTextView.text = dto.type
          positionQtdItemsTextView.text = dto.qtdItems.toInt().toString()
          positionUnitTextView.text = dto.unity
          /*clickablePosition.setOnClickListener {

              }*/
        }
      }

      this.setAdapter(object : RecyclerView.Adapter<ViewHolder>() {

        var items: List<PickStockPositionDto> = item.stockPositions

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
          ViewHolder(parent.inflateToViewHolder(R.layout.item_pick_stock_product_position))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) =
          holder.bind(items[position])

        override fun getItemCount() = items.size

      })
    }
  }

  private fun createPickItemLayout(item: PickingItemDto): View {
    return inflate<View>(R.layout.item_picking_product_qtd_details).apply {
      findViewById<TextView>(R.id.product_name).text = item.name
      //findViewById<TextView>(R.id.product_position).text = item.position
      findViewById<TextView>(R.id.product_sku).text = item.sku
      findViewById<TextView>(R.id.product_gtin).text = coalesce(item.gtin, R.string.no_gtin)
    }
  }

  class PickingItemsAdapter(private val onClickPickingItem: OnClickItem) :
    RecyclerView.Adapter<PickingItemsAdapter.PickingItemViewHolder>() {

    var items: List<PickingItemDto> = mutableListOf()
      set(value) {
        field = value
        //notifyDataSetChanged()
      }

    class PickingItemViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
      private var productPositionTextView = view.findViewById<TextView>(R.id.position_name)
      private var productNameTextView = view.findViewById<TextView>(R.id.product_name)
      private var gtinTextView = view.findViewById<TextView>(R.id.gtin_text)
      private var skuTextView = view.findViewById<TextView>(R.id.sku_text)
      private var storageUnitTextView = view.findViewById<TextView>(R.id.storage_unit_text)
      private var pickedQtdTextView = view.findViewById<TextView>(R.id.picked_qtd)
      private var pickRepositionRequested =
        view.findViewById<TextView>(R.id.reposition_requested_text)

      private var storageUnitCodeTextView = view.findViewById<TextView>(R.id.storage_unit_text)
      private var pickingStatusBadge = view.findViewById<Badge>(R.id.picking_status)
      private var itemToClick = view.findViewById<View>(R.id.pick_item_to_click)

      fun bind(
        index: Int,
        item: PickingItemDto,
        onClickPickingItem: OnClickItem
      ) {
        productPositionTextView.text = "L${index + 1} - ${item.position}"
        productNameTextView.text = item.name
        skuTextView.text = coalesce(item.sku, R.string.no_sku)
        gtinTextView.text = coalesce(item.gtin, R.string.no_gtin)
        storageUnitCodeTextView.text = item.getUnitCode()
        pickRepositionRequested.setVisible(item.hasRequestedPickingReposition)

        pickingStatusBadge.text = item.status.name
        pickingStatusBadge.backgroundColor = item.status.color
        storageUnitTextView.setVisible(item.storageUnit != null)

        /*countHistoryTextView.setVisible(item.isCounted())
        countHistoryTextView.setOnClickListener {
          onClickPickingItem.onClickHistory(item)
        }
        countHistoryTextView.text = view.context.getString(R.string.historic_qtd, item.counts.size)*/

        item.storageUnit?.let {
          storageUnitTextView.text = it.code
        }

        pickedQtdTextView.text = view.context.getString(
          R.string.one_slash_another,
          item.pickedQuantity?.toInt(),
          item.expectedQuantityToPick?.toInt()
        )

        itemToClick.setOnClickListener {
          onClickPickingItem.onClick(item)
        }
        /*view.setOnLongClickListener {
          onClickPickingItem.onClickHistory(item)
          false
        }*/
      }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PickingItemViewHolder {
      return PickingItemViewHolder(parent.inflateToViewHolder(R.layout.item_picking_layout))
    }

    override fun getItemCount(): Int {
      return items.size
    }

    override fun onBindViewHolder(holder: PickingItemViewHolder, index: Int) {
      val item = items[index]
      holder.bind(index, item, onClickPickingItem)
    }

    interface OnClickItem {
      fun onClick(item: PickingItemDto)
      //fun onClickHistory(item: PickingItemDto)
    }
  }
}
