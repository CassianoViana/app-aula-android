package br.com.trivio.wms.extensions

fun String?.coalesce(alternative: String): String {
  if (this == null || this.isEmpty()) {
    return alternative;
  }
  return this
}

fun String.matchOtherReplacing(other: String, old: String, new: String): Boolean {
  return replace(old, new) == other.replace(old, new)
}

fun String.matchRemovingDots(other: String): Boolean {
  return matchOtherReplacing(other, ".", "")
}
