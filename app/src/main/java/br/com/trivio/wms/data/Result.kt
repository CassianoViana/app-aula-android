package br.com.trivio.wms.data

/**
 * A generic class that holds a value with its loading status.
 * @param <T>
 */
sealed class Result<out T : Any?> {

  data class Success<out T : Any>(val data: T) : Result<T>()
  data class Error(val throwable: Throwable) : Result<Nothing>()
  data class Null<out T : Any?>(val nullable: T?) : Result<T>()

  override fun toString(): String {
    return when (this) {
      is Success<*> -> "Success[data=$data]"
      is Error -> "Error[throwable=$throwable]"
      is Null<*> -> "Null[object=$nullable]"
    }
  }

  companion object {
    fun <T : Any> call(fn: () -> T?): Result<T> {
      return try {
        val data = fn()
        return if (data != null) {
          Success(data)
        } else {
          Null(data)
        }
      } catch (e: Exception) {
        Error(e)
      }
    }
  }
}
