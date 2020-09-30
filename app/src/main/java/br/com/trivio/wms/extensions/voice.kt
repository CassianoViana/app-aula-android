import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log

lateinit var tts: TextToSpeech
var ttsAvailable: Boolean = true
fun textToSpeech(something: String, context: Context, onFinish: () -> Unit = {}) {
  try {
    if (!ttsAvailable) {
      onFinish()
    }
    tts = TextToSpeech(context.applicationContext) {
      if (it == TextToSpeech.SUCCESS) {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
          override fun onStart(p0: String?) {
            Log.i("TTS", "onStart")
            ttsAvailable = false
          }

          override fun onDone(p0: String?) {
            Log.i("TTS", "onDone")
            onFinish()
            tts.stop();
            tts.shutdown()
            ttsAvailable = true
          }

          override fun onError(p0: String?) {
            Log.i("TTS", "onError")
          }
        })
        tts.speak(
          something,
          TextToSpeech.QUEUE_FLUSH,
          null,
          TextToSpeech.ACTION_TTS_QUEUE_PROCESSING_COMPLETED
        )
        print(something)
      }
    }
  } catch (e: Exception) {
    Log.e("SPEECH", e.stackTrace.toString())
  }
}

