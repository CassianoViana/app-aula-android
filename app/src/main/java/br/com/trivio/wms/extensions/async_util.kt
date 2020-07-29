package br.com.trivio.wms.extensions

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

suspend fun <T : Any?> asyncRequest(request: () -> T): T {
  return withContext(Dispatchers.IO) {
    request()
  }
}
