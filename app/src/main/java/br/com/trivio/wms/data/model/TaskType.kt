package br.com.trivio.wms.data.model

import com.fasterxml.jackson.annotation.JsonFormat

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
enum class TaskType(private val description: String) {
  CARGO_CONFERENCE("Conferência"),
  PICKING_CONFERENCE("Conferência de Picking"),
  STORAGE("Armazenamento"),
  PICKING("Separação");
}
