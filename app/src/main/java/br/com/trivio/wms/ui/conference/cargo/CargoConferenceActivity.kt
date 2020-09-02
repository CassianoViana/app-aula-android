package br.com.trivio.wms.ui.conference.cargo

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R
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
      searchProductToCount(item.gtin)
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
    onClickCameraOpenBarcodeReader()
    onReadBarcodeFillSearchInput()
    onClickReaderHideKeyboard()
    loadData()
  }

  private fun onClickReaderHideKeyboard() {
    barcode_reader.setOnClickListener {
      barcode.hideKeyboard()
    }
  }

  override fun onResume() {
    super.onResume()
    resetReadingState()
    if (checkCameraPermissions(Manifest.permission.CAMERA)) {
      barcode_reader.start()
    }
  }

  override fun onPause() {
    super.onPause()
    barcode.setKeyboardVisible(false)
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

  private var waiting = false
  private fun onReadBarcodeFillSearchInput() {
    barcode_reader.onRead = { it ->
      runOnUiThread {
        playAudio(this, R.raw.beep)
        if (!waiting) {
          waiting = true
          barcode.applySearch(it)
          delay(1000) {
            waiting = false
            barcode_reader.start()
          }
        }
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
      barcode.hideKeyboard()
    }
  }

  private fun onClickFinish() {
    btn_finish_arrival.setOnClickListener {
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
          if (result.data.isPending()) {
            viewModel.startCounting(cargoConferenceTaskId)
            loadCargoConferenceTask()
          }
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
        clearTop()
      })
    })
  }

  private fun onBarcodeChangeSearchProduct() {
    barcode.addOnSearchListener { gtin ->
      searchProductToCount(gtin)
    }
  }

  private fun loadData() {
    lifecycleScope.launchWhenCreated {
      loadCargoConferenceTask()
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
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
    val totalCountedItems = data.totalCountedItems
    val totalDivergentItems = data.getTotalDivergentItems()
    val statusCounting = data.getStatusCounting()

    val restartString = if (data.restartCounter != null && data.restartCounter > 0) {
      getString(R.string.counting_number, data.restartCounter + 1)
    } else {
      ""
    }
    val labelStatusCounting =
      getString(
        R.string.truck_label__one_from_max_counted,
        "${data.cargoReferenceCode} $restartString",
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

    progress_bar.setStatus(progressStatus)
    progress_bar.setText(labelStatusCounting)
    progress_bar.setProgress(data.getPercentProgress())
  }

  private fun updateBtnFinishTask(data: CargoConferenceDto) {
    val statusCounting = data.getStatusCounting()
    /*btn_finish_conference.setVisible(statusCounting == STATUS_COUNTING_ALL_COUNTED)*/
  }

  private fun loadCargoConferenceTask() {
    startLoading()
    viewModel.loadCargoConferenceTask(cargoConferenceTaskId)
  }

  private fun searchProductToCount(gtin: String) {
    lifecycleScope.launch {
      onResult(viewModel.getCargoItem(gtin),
        onSuccess = {
          openCountItemDialog(it.data)
        },
        onNullResult = {
          showMessageError(getString(R.string.not_found_product_with_gtin, gtin))
        },
        onError = { resetReadingState() }
      )
    }
  }

  private fun openCountItemDialog(item: CargoConferenceItemDto) {

    val totalQuantityCounted = item.countedQuantity?.toInt()
    var unitName = getString(R.string.unit)
    item.storageUnit?.let {
      unitName = it.description
    }
    val countedQtdNumberInput =
      createInputNumber(BigDecimal.ZERO, getString(R.string.counted_units, unitName))

    val viewsToAdd = mutableListOf<View>()
    if (totalQuantityCounted != null) {
      if (totalQuantityCounted > 0) {
        val labelWithCountedQtd =
          createTextView(
            getString(
              R.string.already_conted_x_unities,
              totalQuantityCounted,
              unitName.toLowerCase()
            )
          )
        viewsToAdd.add(labelWithCountedQtd)
      }
    }
    prompt(
      firstTitle = getString(R.string.inform_qtds),
      secondTitle = item.name,
      inputValue = totalQuantityCounted,
      hint = "0",
      inputView = countedQtdNumberInput,
      viewsToAdd = viewsToAdd,
      negativeButtonText = getString(R.string.inform_damage),
      positiveAction = fun(dialog: Dialog, _) {
        countedQtdNumberInput.value?.let { quantity ->
          viewModel.countItem(item, quantity)
          resetReadingState()
          dialog.hide()
        }
      },
      negativeAction = {
        openReportDamageDialog(item)
      },
      closeAction = {
        resetReadingState()
        showMessageInfo(R.string.cancelled_operation)
      }
    )
  }

  private fun resetReadingState() {
    barcode.reset()
    barcode_reader.start()
  }

  private fun openReportDamageDialog(item: CargoConferenceItemDto) {

    val damageDto = item.damage ?: DamageDto()
    val damageCountInput = createInputNumber()
    item.damage?.let {
      damageCountInput.setValue(it.quantity)
    }


    // Quantos itens foram quebrados?
    prompt(
      firstTitle = getString(R.string.how_many_items_are_damaged),
      secondTitle = item.name,
      inputView = damageCountInput,
      positiveAction = { _, _: String ->
        damageDto.quantity = damageCountInput.value

        // Descreva o que aconteceu
        prompt(
          firstTitle = getString(R.string.describe_damage),
          secondTitle = getString(R.string.describe_the_damage_template, item.name),
          positiveAction = { _, value ->
            damageDto.description = value
            damageDto.cargoItemId = item.cargoItemId
            viewModel.registerDamage(damageDto)
          },
          inputType = InputType.TYPE_CLASS_TEXT,
          inputValue = damageDto.description,
          hint = getString(R.string.damage_example),
          negativeButtonText = getString(R.string.inform_damage)
        )
      },

      inputValue = damageDto.quantity?.toInt(),
      negativeButtonText = getString(R.string.inform_damage)
    )
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
      private var storageUnit = view.findViewById<TextView>(R.id.storage_unit_text)
      private var countedQtd = view.findViewById<TextView>(R.id.counted_qtd)
      private var damagedQtd = view.findViewById<TextView>(R.id.damaged_qtd)
      private var labelItems = view.findViewById<TextView>(R.id.label_items)
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
        countedQtd.setVisible(status.icon != null)
        damagedQtd.setVisible(item.damage != null)
        storageUnit.setVisible(item.storageUnit != null)

        item.damage?.let {
          damagedQtd.text = view.context.getString(R.string.damaged_items, it.quantity?.toInt())
        }

        item.storageUnit?.let {
          storageUnit.text = it.code
        }

        labelItems.text = view.context.getString(
          if (status.icon == null) {
            R.string.not_counted
          } else {
            R.string.items
          }
        )
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
