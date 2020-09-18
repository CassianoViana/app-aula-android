package br.com.trivio.wms.data.dto

import br.com.trivio.wms.extensions.matchFilter

class EquipmentDto(
  val id: Long = 0,
  val name: String? = null,
  var code: String? = null
) {

  fun search(filterString: String): Boolean {
    return matchFilter(this.toString(), filterString)
  }

  override fun toString(): String {
    return "EquipmentDto(id=$id, name=$name)"
  }
}
