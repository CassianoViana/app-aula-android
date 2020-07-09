package br.com.trivio.wms.data

import br.com.trivio.wms.data.Result.Success
import br.com.trivio.wms.data.dto.CargoListDto

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any> {

  data class Success<out T : Any>(val data: T) : Result<T>()
  data class Error(val throwable: Throwable) : Result<Nothing>()

  override fun toString(): String {
    return when (this) {
      is Success<*> -> "Success[data=$data]"
      is Error -> "Error[throwable=$throwable]"
    }
  }

  companion object {
    fun <T : Any> call(listCargos: () -> T): Result<T> {
      return try {
        Success(listCargos())
      } catch (e: Exception) {
        Error(e)
      }
    }
  }
}
