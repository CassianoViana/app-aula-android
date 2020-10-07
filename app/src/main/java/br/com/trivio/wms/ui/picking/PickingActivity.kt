package br.com.trivio.wms.ui.picking

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
import br.com.trivio.wms.components.custom.*
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
import br.com.trivio.wms.ui.equipments.ConfirmEquipmentsListActivity
import br.com.trivio.wms.ui.equipments.ReleaseEquipmentsListActivity
import br.com.trivio.wms.viewmodel.picking.PickingViewModel
import kotlinx.android.synthetic.main.activity_picking.*
import java.math.BigDecimal


class PickingActivity : MyAppCompatActivity() {

  companion object {
    const val PICKING_TASK_ID: String = "PICKING_TASK_id"
    const val RESULT_TASK_ID: Int = 100
  }

  private var pickingTaskId: Long = 0
  private var pickingItemsAdapter = PickingItemsAdapter(object : PickingItemsAdapter.OnClickItem {
    override fun onClick(item: PickingItemDto) {
      openPositionDialog(item)
    }

    override fun onLongClick(item: PickingItemDto) {
      openDevolutionDialog(item)
    }

    /*override fun onClickHistory(item: PickingItemDto) {
      openEquipmentsListActivity(item)
    }*/

  })

  private fun openDevolutionDialog(item: PickingItemDto) {
    openCustomContextMenu(
      this,
      title = getString(R.string.actions),
      mutableListOf(
        MenuItem(getString(R.string.devolve_qtds)) {
          if (item.hasItemsPicked()) {
            openPositionDialog(
              item,
              callbackOnValidPosition = { dialogPosition ->
                openDevolveItemDialog(item)
                dialogPosition.hide()
              },
              title = getString(R.string.confirm_position_to_devolve)
            )
          } else {
            showMessageError(R.string.the_item_has_no_qtd_to_devolve)
          }
        },
      )
    )
  }

  private val viewModel: PickingViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_picking)
    pickingTaskId = intent.getLongExtra(PICKING_TASK_ID, 0)
    picking_items_recycler_view.setAdapter(pickingItemsAdapter)
    observeViewModel()
    onRefreshLoadData()
    onClickFinishGoToReleaseEquipmentsActivity()
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

  private fun openPositionDialog(
    item: PickingItemDto,
    title: String? = null,
    callbackOnValidPosition: ((Dialog) -> Unit)? = null,
    dialogToReuse: Dialog? = null
  ) {
    val onValidPositionCode: (Dialog) -> Unit = callbackOnValidPosition ?: { positionDialog ->
      openProductDialog(item,
        onCloseProductDialogListener = {
          viewModel.findNextItem(item) { it ->
            onResult(it,
              onSuccess = {
                openPositionDialog(
                  it.data,
                  dialogToReuse = positionDialog
                )
              },
              onNullResult = {
                viewModel.loadPickingTask(pickingTaskId) { result ->
                  onResult(result, onSuccess = { success ->
                    val pickingTask = success.data
                    if (pickingTask.items.all { item -> item.hasItemsPicked() }) {
                      showMessageSuccess(R.string.all_items_were_picked)
                    } else {
                      showMessageSuccess(
                        getString(
                          R.string.x_items_were_not_picked_yet,
                          pickingTask.countItemsNotPicked
                        ),
                        say = true
                      )
                    }
                  })
                }
                positionDialog.hide()
              }
            )
          }
        })
    }
    val barcodeReader = createBarcodeReader("R00.B00.A00.L00")
    val positionInput = barcodeReader.getInput()
    prompt(
      dialog = dialogToReuse,
      firstTitle = title ?: getString(
        R.string.confirm_position_x_of_total,
        item.order,
        item.totalItemsTask
      ),
      viewsBeforeInput = listOf(createTextView(item.position, large = true)),
      hideDefaultInputView = true,
      inputViewFn = { dialog ->
        barcodeReader.apply {
          val positionOnlyContainsNumbers = item.position.matches(Regex("[0-9.]+"))
          if (!positionOnlyContainsNumbers)
            setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD)
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
      viewsBeforeInput = listOf(createPickItemLayout(item, showQuantities = true)),
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
      drawableConfirmButtonResId = R.drawable.ic_baseline_arrow_forward_18,
      positiveAction = { dialog, productCode ->
        validateProductCode(item, productCode,
          onValid = { onValidProductCode(dialog) },
          onInvalid = {
            productInput.selectAll()
          })
      },
      negativeButtonText = getString(R.string.check_stock),
      negativeAction = {
        openStockDialog(item)
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

  override fun onFinish() {
    val data = Intent()
    data.putExtra(PICKING_TASK_ID, this.pickingTaskId)
    setResult(RESULT_TASK_ID, data)
    super.onFinish()
  }

  override fun onResume() {
    super.onResume()
    resetReadingState()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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

  private fun openEquipmentsListActivity() {
    val intent = Intent(this, ConfirmEquipmentsListActivity::class.java)
    intent.putExtra(ConfirmEquipmentsListActivity.PICKING_TASK_ID, pickingTaskId)
    startActivity(intent)
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
    if (cargoConferenceDto.taskStatus != TaskStatus.DONE) {
      updateStatusCargo(cargoConferenceDto)
    }
    endLoading()
  }

  private fun updateCargoItems(items: List<PickingItemDto>) {
    pickingItemsAdapter.items = items
    pickingItemsAdapter.notifyDataSetChanged()
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
        pickingTaskDto.quantityCompletelyPickedItems,
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

  private fun openPickItemDialog(
    item: PickingItemDto,
    onFinishPickItem: (item: PickingItemDto) -> Any? = {}
  ) {
    val unitCode: String? = item.getUnitCode(getString(R.string.unit_code))
    val inputNumber = createInputNumber(
      labelBeforeInput = getString(R.string.separadas),
      labelAfterInput = unitCode,
      allowNegative = false,
    )
    prompt(
      firstTitle = getString(R.string.inform_picked_qtd),
      hint = "0",
      viewsBeforeInput = listOf(createPickItemLayout(item, showQuantities = true)),
      inputViewFn = {
        inputNumber
      },
      negativeButtonText = getString(R.string.check_stock),
      positiveButtonText = getString(R.string.next),
      drawableConfirmButtonResId = R.drawable.ic_baseline_arrow_forward_18,
      positiveAction = fun(dialog: Dialog, _) {
        inputNumber.value?.let { quantity ->
          if (quantity <= BigDecimal.ZERO) {
            showMessageError(R.string.the_quantity_cannot_be_zero)
            inputNumber.showError()
            return
          }
          viewModel.pickItem(
            item = item,
            quantity = quantity,
            position = item.position
          ) { result ->
            onResult(result, onSuccess = {
              showMessageSuccess(R.string.item_separed_with_success, say = true) {
                onFinishPickItem(item)
                delay {
                  dialog.hide()
                }
              }
            })
          }
        }
      },
      negativeAction = { dialog ->
        openStockDialog(item)
      },
    )
  }

  private fun openDevolveItemDialog(
    item: PickingItemDto,
    onFinishPickItem: (item: PickingItemDto) -> Any? = {}
  ) {
    val unitCode: String? = item.getUnitCode(getString(R.string.unit_code))
    val inputNumber = createInputNumber(
      labelBeforeInput = getString(R.string.devolve),
      labelAfterInput = unitCode,
      allowNegative = false,
    )
    prompt(
      firstTitle = getString(R.string.inform_qtd_to_devolve),
      hint = "0",
      viewsBeforeInput = listOf(createPickItemLayout(item, showQuantities = true)),
      inputViewFn = {
        inputNumber
      },
      positiveButtonText = getString(R.string.devolve),
      positiveAction = fun(dialog: Dialog, _) {
        inputNumber.value?.let { quantity ->
          if (quantity <= BigDecimal.ZERO) {
            showMessageError(R.string.the_quantity_cannot_be_zero)
            inputNumber.showError()
            return
          }
          viewModel.devolveItem(item, quantity) { it ->
            onResult(it, onSuccess = {
              showMessageSuccess(
                getString(R.string.the_item_was_devolved, quantity.toInt()),
                say = true
              ) {
                onFinishPickItem(item)
                delay {
                  dialog.hide()
                }
              }
            })
          }
        }
      },
    )
  }

  @Deprecated("O campo será removido")
  private fun resetReadingState() {
    //barcode_search_input.reset()
    //barcode_reader.startRead()
  }

  private fun openStockDialog(
    item: PickingItemDto,
    //onPositionSelected: (item: PickingItemDto) -> Unit = {}
  ) {
    val stockList = createStockList(item).apply { setWeight(1) }
    prompt(
      firstTitle = getString(R.string.product_stock),
      viewsBeforeInput = listOf(createPickItemLayout(item)),
      hideDefaultInputView = true,
      viewsAfterInputFn = { _, views ->
        views.add(stockList)
        if (item.hasRequestedPickingReposition) {
          views.add(Message(this).apply {
            setType(Message.TYPE_WARNING)
            message = getString(R.string.already_exists_reposition_request)
            setMarginVertical()
          })
        }
      },
      positiveButtonText = getString(
        if (item.hasRequestedPickingReposition) R.string.cancel_request_picking_reposition
        else R.string.request_picking_reposition
      ),
      negativeButtonText = getString(R.string.not_localized),
      positiveAction = { dialog, _: String ->
      },
      negativeAction = {

      },
    )
  }

  private fun createStockList(item: PickingItemDto): RefreshableList {
    return inflate<RefreshableList>(R.layout.pick_item_stock_positions_list).apply {
      setMarginVertical()

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
          positionQtdItemsTextView.text = dto.qtdItems?.toInt().toString()
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

  private fun createPickItemLayout(item: PickingItemDto, showQuantities: Boolean = false): View {
    return inflate<View>(R.layout.item_picking_product_qtd_details).apply {
      findViewById<TextView>(R.id.product_name).text = item.name
      //findViewById<TextView>(R.id.product_position).text = item.position
      findViewById<TextView>(R.id.product_sku).text = item.sku
      findViewById<TextView>(R.id.product_gtin).text = coalesce(item.gtin, R.string.no_gtin)
      findViewById<LabelledInfo>(R.id.text_view_qtd_solicited).apply {
        setVisible(showQuantities)
        text =
          "${item.pickedQuantity?.toInt()} / ${item.expectedQuantityToPick?.toInt()}  ${item.getUnitCode()}"
      }
    }
  }

  class PickingItemsAdapter(private val onClickPickingItem: OnClickItem) :
    RecyclerView.Adapter<PickingItemsAdapter.PickingItemViewHolder>() {

    var items: List<PickingItemDto> = mutableListOf()

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
      fun onLongClick(item: PickingItemDto)
      //fun onClickHistory(item: PickingItemDto)
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
        onClickPickingItem: PickingItemsAdapter.OnClickItem
      ) {
        productPositionTextView.text =
          view.context.getString(R.string.locale_item_picking, index + 1, item.position)
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

        pickedQtdTextView.text = if (item.pickedQuantity != null) {
          view.context.getString(
            R.string.one_slash_another,
            item.pickedQuantity?.toInt(),
            item.expectedQuantityToPick?.toInt()
          )
        } else {
          item.expectedQuantityToPick?.toInt().toString()
        }

        itemToClick.setOnLongClickListener {
          onClickPickingItem.onLongClick(item)
          true
        }
        itemToClick.setOnClickListener {
          onClickPickingItem.onClick(item)
        }

        /*view.setOnLongClickListener {
          onClickPickingItem.onClickHistory(item)
          false
        }*/
      }
    }
  }
}
