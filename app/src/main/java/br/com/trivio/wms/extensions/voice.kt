import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.*

lateinit var tts: TextToSpeech
fun textToSpeech(something: String, context: Context) {
  tts = TextToSpeech(context) {
    if (it == TextToSpeech.SUCCESS) {
      tts.speak(something, TextToSpeech.QUEUE_FLUSH, null)
      print(something)
    }
  }
}
