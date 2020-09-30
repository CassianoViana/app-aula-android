package br.com.trivio.wms.extensions

import android.util.Log
import me.xdrop.fuzzywuzzy.FuzzySearch
import java.util.*

fun stringSimilarity(originalString: String, comparisonString: String): Int {
  //val algorithm = LongestCommonSubsequence()
  val distance = FuzzySearch.partialRatio(originalString, comparisonString)
  Log.i("DISTANCE", "$originalString, $comparisonString, $distance")
  return distance
}

fun matchFilter(originalString: String, comparisonString: String): Boolean {
  //return originalString.contains(comparisonString)
  val a = originalString.toUpperCase(Locale.getDefault()).replace(".", " ")
  val b = comparisonString.toUpperCase(Locale.getDefault())
  return stringSimilarity(a, b) >= 80 || a.contains(b)
}

fun String.similarity(another: String): Int {
  var similarity = stringSimilarity(this, another);
  if (similarity < 80) {
    if (this.contains(another)) {
      similarity = 50
    }
  }
  return similarity
}

fun String.isVerySimilar(another: String): Boolean {
  return matchFilter(this, another)
}
