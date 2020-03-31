package br.com.trivio.wms.ui.conference.cargo

import android.content.DialogInterface.BUTTON_POSITIVE
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import br.com.trivio.wms.*
import br.com.trivio.wms.data.dto.CargoConferenceDto
import br.com.trivio.wms.data.dto.CargoConferenceItemDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class CargoConferenceActivity : AppCompatActivity() {

  companion object {
    const val CARGO_ID: String = "CARGO_id"
  }

  private lateinit var barcode: EditText
  private var cargoConferenceId: Long = 0

  private val viewModel: CargoConferenceViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_cargo_conference)
    this.setupToolbar(R.string.cargo_conference)
    cargoConferenceId = intent.getLongExtra(CARGO_ID, 0)
    barcode = findViewById(R.id.barcode)
    observeViewModel()
    onClickSearchOpenItemsActivity()
    onBarcodeChangeSearchProduct()
    loadTaskOnCreate()
    showKeyboard(barcode)
  }

  private fun observeViewModel() {
    viewModel.cargoItem.observe(this, Observer {
      threatResult(it,
        onSuccess = {
          endLoading()
          showMessageSuccess(R.string.counted_success)
          loadTask()
        })
    })
    viewModel.task.observe(this, Observer {
      threatResult(it, onSuccess = { result ->
        updateUi(result.data)
      })
    })
  }

  private fun updateUi(data: CargoConferenceDto) {
    val of = getString(R.string.of)
    val progress = "${data.getTotalCountedItems()} $of ${data.items.size}"
    findViewById<TextView>(R.id.qtd_items_to_count).text = progress
    findViewById<View>(R.id.totals_to_count).setBackgroundColor(
      getColor(
        when (data.getStatusCounting()) {
          CargoConferenceDto.STATUS_COUNTING_ALL_COUNTED -> R.color.success
          CargoConferenceDto.STATUS_COUNTING_NONE_COUNTED -> R.color.colorAccent
          else -> R.color.colorBlueAccent
        }
      )
    )
  }

  private fun onBarcodeChangeSearchProduct() {
    barcode.addTextChangedListener {
      val gtin = it.toString()
      if (gtin.length >= 5) {
        loadItemToRequestQuantity(gtin)
      }
    }
  }

  private fun onClickSearchOpenItemsActivity() {
    val buttonSearchProduct = findViewById<Button>(R.id.btn_search_product)
    buttonSearchProduct.setOnClickListener {
      startSearchProductsActivity()
    }
  }

  private fun loadTaskOnCreate() {
    lifecycleScope.launchWhenCreated {
      loadTask()
    }
  }

  private fun loadTask() {
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
      barcode.text.toString()
    )
    hideKeyboard(barcode)
    startActivityForResult(searchProductIntent, ConferenceItemsActivity.REQUEST_CODE_SELECT_ITEM)
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

  private fun loadItemToRequestQuantity(gtin: String) {
    lifecycleScope.launch {
      val cargoConferenceItem = withContext(Dispatchers.IO) {
        viewModel.getCargoItem(gtin)
      }
      threatResult(cargoConferenceItem, onSuccess = {
        requestQuantity(it.data)
      })
    }
  }

  private fun requestQuantity(item: CargoConferenceItemDto) {
    val input = inflate<EditText>(R.layout.input_quantity)
    val resetBarcode = fun() {
      hideKeyboard(input)
      showKeyboard(barcode)
      barcode.setText("");
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

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item) {
      hideKeyboard(barcode)
    }
    return super.onOptionsItemSelected(item)
  }
}
