package br.com.trivio.wms.components.custom

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import br.com.trivio.wms.R
import br.com.trivio.wms.extensions.setVisible
import kotlinx.android.synthetic.main.custom_refreshable_list.view.*


class RefreshableList @JvmOverloads constructor(
  context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

  fun setOnRefreshListener(onRefresh: () -> Unit) {
    refreshLayout.setOnRefreshListener {
      onRefresh()
    }
  }

  fun setLoading(loading: Boolean) {
    refresh_progress_bar.setVisible(loading)
  }

  fun stopRefresh() {
    refreshLayout.isRefreshing = false
    setLoading(false)
  }

  fun setHorizontal() {
    val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
    recyclerView.layoutManager = layoutManager
  }

  fun <T : RecyclerView.ViewHolder> setAdapter(adapter: RecyclerView.Adapter<T>) {
    recyclerView.adapter = adapter
    adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
      override fun onChanged() {
        super.onChanged()
        showEmptyLabel(adapter.itemCount == 0)
      }
    })
  }

  private var refreshLayout: SwipeRefreshLayout
  private var recyclerView: RecyclerView

  private fun showEmptyLabel(show: Boolean) {
    empty_list_label.setVisible(show)
  }

  fun scrollTop() {
    recyclerView.smoothScrollToPosition(0)
  }

  init {
    LayoutInflater.from(context).inflate(R.layout.custom_refreshable_list, this, true)
    refreshLayout = findViewById(R.id.swipe_refresh)
    recyclerView = findViewById(R.id.recycler_view)
    showEmptyLabel(false)
    recyclerView.layoutManager = LinearLayoutManager(context)

    attrs?.let {
      val styledAttributes = context.obtainStyledAttributes(it, R.styleable.RefreshsableList)
      val refreshable = styledAttributes.getBoolean(R.styleable.RefreshsableList_refreshable, true)
      val horizontal = styledAttributes.getBoolean(R.styleable.RefreshsableList_horizontal, false)

      if (!refreshable) {
        setOnRefreshListener {
          stopRefresh()
        }
      }

      if (horizontal) {
        setHorizontal()
      }

      styledAttributes.recycle()

    }

  }

}
