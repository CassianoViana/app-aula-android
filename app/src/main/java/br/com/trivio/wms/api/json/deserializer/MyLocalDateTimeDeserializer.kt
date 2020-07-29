package br.com.trivio.wms.api.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.ISODateTimeFormat

private val formatters = mapOf<String, DateTimeFormatter>(
  "[0-9]{4}-[0-9]{2}-[0-9]{2}" to ISODateTimeFormat.date(),
  "[0-9]{4}-[0-9]{2}-[0-9]{2}'T'[0-9]{2}:[0-9]{2}" to ISODateTimeFormat.dateHourMinute(),
  "[0-9]{2}/[0-9]{2}/[0-9]{4}" to DateTimeFormat.forPattern("dd/MM/yyyy"),
  "[0-9]{2}/[0-9]{2}/[0-9]{4} [0-9]{2}:[0-9]{2}" to DateTimeFormat.forPattern("dd/MM/yyyy HH:mm")
)

class MyLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {

  override fun deserialize(parser: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
    return when (parser!!.currentTokenId) {
      JsonTokenId.ID_STRING -> {
        parseStringDate(parser)
      }
      else -> null
    }
  }

  private fun parseStringDate(parser: JsonParser): LocalDateTime? {
    var localDateTime1 = LocalDateTime()
    val dateTimeString = parser.text.trim { it <= ' ' }
    getFormatter(dateTimeString)?.let { compatibleFormatter ->
      localDateTime1 = compatibleFormatter.parseLocalDateTime(dateTimeString)
    }
    return localDateTime1
  }

  private fun getFormatter(dateTimeString: String): DateTimeFormatter? {
    return formatters.entries.firstOrNull {
      val regex: String = it.key
      dateTimeString.matches(Regex(regex))
    }?.value
  }

}
