package br.com.trivio.wms.data.dto

class EquipmentDto(
  val id: Long = 0,
  val name: String? = null,
  var code: String? = null,
  var selected: Boolean
) {

  override fun toString(): String {
    return "EquipmentDto(id=$id, name=$name, code=$code)"
  }
}
