package br.com.trivio.wms.extensions

import android.content.Context
import android.media.MediaPlayer

fun playAudio(context: Context, beep: Int) {
  val mediaPlayer = MediaPlayer.create(context, beep)
  mediaPlayer.start()
  mediaPlayer.setOnSeekCompleteListener {
    mediaPlayer.release()
  }
}
