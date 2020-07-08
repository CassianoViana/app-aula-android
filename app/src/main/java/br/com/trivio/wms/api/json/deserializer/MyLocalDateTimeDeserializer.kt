package br.com.trivio.wms.api.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
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
      JsonTokenId.ID_START_ARRAY -> {
        parseArrayDate(parser, ctxt)
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

  private fun parseArrayDate(
    parser: JsonParser,
    deserializationContext: DeserializationContext?
  ): LocalDateTime? {
    // [yyyy,mm,dd,hh,MM,ss,ms]
    var t = parser.nextToken()
    var dt: LocalDateTime? = null
    do {
      if (!t.isNumeric) {
        break
      }
      val year = parser.intValue
      t = parser.nextToken()
      if (!t.isNumeric) {
        break
      }
      val month = parser.intValue
      t = parser.nextToken()
      if (!t.isNumeric) {
        break
      }
      val day = parser.intValue
      t = parser.nextToken()
      if (!t.isNumeric) {
        break
      }
      val hour = parser.intValue
      t = parser.nextToken()
      if (!t.isNumeric) {
        break
      }
      val minute = parser.intValue
      t = parser.nextToken()
      if (!t.isNumeric) {
        break
      }
      val second = parser.intValue
      t = parser.nextToken()
      // let's leave milliseconds optional?
      var millisecond = 0
      if (t.isNumeric) {
        millisecond = parser.intValue
        t = parser.nextToken() // END_ARRAY?
      }
      if (millisecond > 999)
        millisecond = 999
      dt = LocalDateTime(year, month, day, hour, minute, second, millisecond)
    } while (false) // bogus loop to allow break from within
    if (t == JsonToken.END_ARRAY) {
      return dt
    }
    throw deserializationContext!!.wrongTokenException(
      parser,
      handledType(),
      JsonToken.END_ARRAY,
      "after LocalDateTime ints"
    )
  }

  private fun getFormatter(dateTimeString: String): DateTimeFormatter? {
    return formatters.entries.firstOrNull {
      val regex: String = it.key
      dateTimeString.matches(Regex(regex))
    }?.value
  }

}
