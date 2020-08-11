import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.widget.TextView
import br.com.trivio.wms.data.dto.TaskDto

object UiUtils {
  fun setTaskStatusStyle(textView: TextView, taskDto: TaskDto) {
    textView.text = taskDto.statusDto?.name
    textView.setTagBackground(taskDto.statusDto?.color)
  }
}

fun TextView.setTagBackground(color: String?) {
  color?.let {
    val gradientDrawable = background as GradientDrawable
    gradientDrawable.cornerRadius = 100f
    try {
      gradientDrawable.setColor(Color.parseColor(it))
    } catch (e: Exception) {
      e.printStackTrace();
      gradientDrawable.setColor(Color.parseColor("#EEEEEE"))
    }
  }
}
