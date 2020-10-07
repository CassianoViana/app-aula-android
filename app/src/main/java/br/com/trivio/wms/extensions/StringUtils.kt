package br.com.trivio.wms.extensions

import java.util.*

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
  return this.toUpperCase(Locale.getDefault()).matchOtherReplacing(
    other.toUpperCase(Locale.getDefault()),
    ".",
    ""
  )
}
