package br.com.trivio.wms.ui.conference.cargo

import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import br.com.trivio.wms.*
import br.com.trivio.wms.data.Result
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import br.com.trivio.wms.data.model.TaskStatus
import br.com.trivio.wms.ui.tasks.TaskDetailsActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class CargoConferenceActivity : AppCompatActivity() {

  companion object {
    const val CARGO_ID: String = "CARGO_id"
  }

  private lateinit var labelCountingStatus: TextView
  private lateinit var labelQtdItemsToCount: TextView
  private lateinit var btnFinishTask: Button
  private lateinit var totalsToCount: LinearLayout
  private lateinit var btnSearchProduct: Button
  private lateinit var editBarcode: EditText
  private var cargoConferenceId: Long = 0

  private val viewModel: CargoConferenceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargo_conference)
    this.setupToolbar(R.string.cargo_conference)

    cargoConferenceId = intent.getLongExtra(CARGO_ID, 0)
    editBarcode = findViewById(R.id.barcode)
    btnSearchProduct = findViewById(R.id.btn_search_product)
    totalsToCount = findViewById(R.id.totals_to_count)
    btnFinishTask = findViewById(R.id.layout_btn_finish)
    labelQtdItemsToCount = findViewById(R.id.qtd_items_to_count)
    labelCountingStatus = findViewById(R.id.label_status_counting)

    observeViewModel()
    onClickSearchOpenItemsActivity()
    onBarcodeChangeSearchProduct()
    onClickFinishTaskEndActivity()
    loadTaskOnCreate()
  }

  private fun observeViewModel() {
    viewModel.cargoItem.observe(this, Observer {
      threatResult(it,
        onSuccess = {
          showMessageSuccess(R.string.counted_success)
          loadTask()
        },
        always = {
          endLoading()
        })
    })
    viewModel.task.observe(this, Observer {
      threatResult(it, onSuccess = { result ->
        updateUi(result.data)
      })
    })
    viewModel.finishStatus.observe(this, Observer {
      threatResult(it, onSuccess = {
        setResult(TaskDetailsActivity.RESULT_TASK_CHANGED)
        finish()
      })
    })
  }

  private fun onClickSearchOpenItemsActivity() {
    val onClickListener = View.OnClickListener {
      startSearchProductsActivity()
    }
    btnSearchProduct.setOnClickListener(onClickListener)
    totalsToCount.setOnClickListener(onClickListener)
  }

  private fun onBarcodeChangeSearchProduct() {
    editBarcode.addTextChangedListener {
      val gtin = it.toString()
      if (gtin.length >= 5) {
        loadItemToRequestQuantity(gtin)
      }
    }
  }

  private fun onClickFinishTaskEndActivity() {
    btnFinishTask
      .setOnClickListener {
        lifecycleScope.launch {
          viewModel.finishTask(cargoConferenceId)
        }
      }
  }

  private fun loadTaskOnCreate() {
    lifecycleScope.launchWhenCreated {
      loadTask()
    }
  }

  private fun updateUi(cargoConferenceDto: CargoConferenceDto) {
    updateUiLabelItemsCounted(cargoConferenceDto)
    if (cargoConferenceDto.taskStatus == TaskStatus.DONE) {
      updateUiDisableControls()
    } else {
      updateUiShowHIdeBtnFinish(cargoConferenceDto)
      updateKeyboardStatusCargo(cargoConferenceDto)
    }
    endLoading()
  }

  private fun updateUiDisableControls() {
    editBarcode.isEnabled = false
    btnSearchProduct.isEnabled = false
  }

  private fun updateKeyboardStatusCargo(dto: CargoConferenceDto) {
    if (dto.getStatusCounting() != CargoConferenceDto.STATUS_COUNTING_ALL_COUNTED) {
      showKeyboard(editBarcode)
    } else {
      hideKeyboard(editBarcode)
    }
  }

  private fun updateUiLabelItemsCounted(data: CargoConferenceDto) {
    labelQtdItemsToCount.text =
      getString(R.string.conted_itens_label, data.getTotalCountedItems(), data.items.size)
    val progressColor =
      when (data.getStatusCounting()) {
        CargoConferenceDto.STATUS_COUNTING_ALL_COUNTED -> {
          labelCountingStatus.visibility = View.VISIBLE
          val totalDivergentItems = data.getTotalDivergentItems()
          when {
            totalDivergentItems > 0 -> {
              labelCountingStatus.text = getString(R.string.divergent_itens, totalDivergentItems)
              R.color.error
            }
            else -> {
              labelCountingStatus.text = getString(R.string.every_items_counted_correct)
              R.color.success
            }
          }
        }
        CargoConferenceDto.STATUS_COUNTING_NONE_COUNTED -> {
          labelCountingStatus.visibility = View.GONE
          R.color.colorAccent
        }
        else -> {
          R.color.colorBlueAccent
        }
      }
    setProgressGradient(
      findViewById(R.id.totals_to_count),
      data.getPercentProgress(),
      getColor(progressColor),
      getColor(R.color.colorPrimary)
    )
  }

  private fun updateUiShowHIdeBtnFinish(data: CargoConferenceDto) {
    findViewById<View>(R.id.layout_btn_finish)
      .visibility = when (data.getStatusCounting()) {
      CargoConferenceDto.STATUS_COUNTING_ALL_COUNTED -> {
        View.VISIBLE
      }
      else -> View.GONE
    }
  }

  private fun loadTask() {
    startLoading()
    viewModel.loadTask(cargoConferenceId)
  }

  private fun startSearchProductsActivity() {
    val searchProductIntent = Intent(this, ConferenceItemsActivity::class.java)
    searchProductIntent.putExtra(
      ConferenceItemsActivity.CARGO_CONFERENCE_ID,
      cargoConferenceId
    )
    searchProductIntent.putExtra(
      ConferenceItemsActivity.SEARCH_PRODUCT_GTIN,
      editBarcode.text.toString()
    )
    hideKeyboard(editBarcode)
    startActivityForResult(searchProductIntent, ConferenceItemsActivity.REQUEST_CODE_SELECT_ITEM)
  }

  private fun loadItemToRequestQuantity(gtin: String) {
    lifecycleScope.launch {
      val cargoItemResult = withContext(Dispatchers.IO) {
        viewModel.getCargoItem(gtin)
      }
      threatResult(cargoItemResult,
        onSuccess = { item ->
          requestQuantity(item.data)
        }
      )
    }
  }

  private fun requestQuantity(item: CargoConferenceItemDto) {
    val input = inflate<EditText>(R.layout.input_quantity)
    val resetBarcode = fun() {
      hideKeyboard(input)
      showKeyboard(editBarcode)
      editBarcode.setText("")
    }
    val dialog = AlertDialog.Builder(this)
      .setTitle(R.string.inform_qtd)
      .setMessage(item.name)
      .setView(input)
      .setCancelable(false)
      .setPositiveButton(R.string.ok, null)
      .setNegativeButton(R.string.cancel) { _, _ ->
        resetBarcode()
      }.show()

    dialog.getButton(BUTTON_POSITIVE).setOnClickListener {
      val value: String = input.text.toString()
      validateCountQuantity(value) { quantity ->
        startLoading()
        viewModel.countItem(item, quantity)
        resetBarcode()
        dialog.dismiss()
      }
    }
    showKeyboard(input)
  }

  private fun validateCountQuantity(value: String, onSuccess: (value: BigDecimal) -> Any) {
    try {
      val bigDecimal = BigDecimal(value)
      onSuccess(bigDecimal)
    } catch (e: Exception) {
      showMessageError(R.string.please_check_the_quantity)
    }
  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (resultCode == ConferenceItemsActivity.SUCCESS) {
      if (requestCode == ConferenceItemsActivity.REQUEST_CODE_SELECT_ITEM) {
        data?.let {
          val gtin = data.getStringExtra(ConferenceItemsActivity.GTIN_ID)
          loadItemToRequestQuantity(gtin)
        }
      }
    }
    super.onActivityResult(requestCode, resultCode, data)
  }


  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item) {
      hideKeyboard(editBarcode)
    }
    return super.onOptionsItemSelected(item)
  }
}
