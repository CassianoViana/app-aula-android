package br.com.trivio.wms.api.json.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.core.JsonTokenId
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.datatype.joda.cfg.JacksonJodaDateFormat
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import org.joda.time.format.ISODateTimeFormat

private val format = JacksonJodaDateFormat(ISODateTimeFormat.date())

class MyLocalDateTimeDeserializer : JsonDeserializer<LocalDateTime>() {
  override fun deserialize(p: JsonParser?, ctxt: DeserializationContext?): LocalDateTime? {
    when (p!!.currentTokenId) {
      JsonTokenId.ID_STRING -> {
        val str = p.text.trim { it <= ' ' }
        return if (str.isEmpty()) null else format.createParser(ctxt).parseLocalDateTime(str)
      }
      JsonTokenId.ID_NUMBER_INT -> {
        val tz =
          if (format.isTimezoneExplicit) format.timeZone else DateTimeZone.forTimeZone(ctxt!!.timeZone)
        return LocalDateTime(p.longValue, tz)
      }
      JsonTokenId.ID_START_ARRAY -> {
        // [yyyy,mm,dd,hh,MM,ss,ms]
        var t = p.nextToken()
        var dt: LocalDateTime? = null
        do {
          if (!t.isNumeric) {
            break
          }
          val year = p.intValue
          t = p.nextToken()
          if (!t.isNumeric) {
            break
          }
          val month = p.intValue
          t = p.nextToken()
          if (!t.isNumeric) {
            break
          }
          val day = p.intValue
          t = p.nextToken()
          if (!t.isNumeric) {
            break
          }
          val hour = p.intValue
          t = p.nextToken()
          if (!t.isNumeric) {
            break
          }
          val minute = p.intValue
          t = p.nextToken()
          if (!t.isNumeric) {
            break
          }
          val second = p.intValue
          t = p.nextToken()
          // let's leave milliseconds optional?
          var millisecond = 0
          if (t.isNumeric) { // VALUE_NUMBER_INT
            millisecond = p.intValue
            if (millisecond > 999)
              millisecond = 0;
            t = p.nextToken()
          }
          dt = LocalDateTime(year, month, day, hour, minute, second, millisecond)
        } while (false) // bogus loop to allow break from within
        if (t == JsonToken.END_ARRAY) {
          return dt
        }
        throw ctxt!!.wrongTokenException(
          p,
          handledType(),
          JsonToken.END_ARRAY,
          "after LocalDateTime ints"
        )
      }
      else -> {
      }
    }
    return ctxt!!.handleUnexpectedToken(
      handledType(), p.currentToken, p,
      "expected String, Number or JSON Array"
    ) as LocalDateTime
  }

}
