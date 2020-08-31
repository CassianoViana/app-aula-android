package br.com.trivio.wms.extensions

fun String?.coalesce(alternative: String): String {
  if (this == null || this.isEmpty()) {
    return alternative;
  }
  return this
}
