package br.com.trivio.wms.extensions

import android.R
import android.content.Context
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AlertDialog

class MenuItem(val name: String = "", val execute: () -> Unit) {
  override fun toString() = name
}

fun openCustomContextMenu(context: Context, title: String? = null, items: List<MenuItem>) {
  val list = ListView(context)
  list.adapter = ArrayAdapter(context, R.layout.simple_list_item_1, items)

  val dialog = AlertDialog.Builder(context)
    .setView(list)
    .setCancelable(true)
    .create()

  title?.let {
    dialog.setTitle(title)
  }

  list.setOnItemClickListener { adapterView, view, position, id ->
    dialog.hide()
    items[position].execute()
  }

  dialog.show()
}
