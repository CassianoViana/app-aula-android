package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.trivio.wms.R

class RefreshableList @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  fun setOnRefreshListener(onRefresh: () -> Unit) {
    refreshLayout.setOnRefreshListener {
      onRefresh()
    }
  }

  fun stopRefresh() {
    refreshLayout.isRefreshing = false
  }

  fun <T : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<T>) {
    recyclerView.adapter = adapter
  }

  private var refreshLayout: SwipeRefreshLayout
  private var recyclerView: RecyclerView

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_refreshable_list, this, true)
    refreshLayout = findViewById(R.id.swipe_refresh)
    recyclerView = findViewById(R.id.recycler_view)

    recyclerView.layoutManager = LinearLayoutManager(context)

    attrs?.let {
      /*val styledAttributes = context.obtainStyledAttributes(it, R.styleable.SearchInput)
      val hint = styledAttributes.getString(R.styleable.SearchInput_hint)
      editTextSearch.hint = hint
      styledAttributes.recycle()*/
    }

  }

}