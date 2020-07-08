import android.widget.TextView
import br.com.trivio.wms.data.dto.TaskDto
import br.com.trivio.wms.setTagBackground

object UiUtils {
  fun setTaskStatusStyle(textView: TextView, taskDto: TaskDto) {
    textView.text = taskDto.statusDto?.name
    textView.setTagBackground(taskDto.statusDto?.color)
  }
}
