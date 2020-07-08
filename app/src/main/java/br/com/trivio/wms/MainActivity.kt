package br.com.trivio.wms

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import br.com.trivio.wms.data.model.UserDetails
import br.com.trivio.wms.ui.cargos.CargosActivity
import br.com.trivio.wms.ui.tasks.TasksActivity
import kotlinx.coroutines.delay

class MenuItem(
  val name: Int,
  val icon: Int,
  val actionWhenClicked: () -> Any = {}
)

class MainActivity : MyAppCompatActivity() {

  private lateinit var exitAppHandler: ExitAppHandler
  private lateinit var menusList: RecyclerView
  private val menus = listOf(
    MenuItem(
      R.string.menu_tasks, R.drawable.ic_attach_file_white_24dp
    ) { startActivity(Intent(this, TasksActivity::class.java)) },

    MenuItem(R.string.arrival, R.drawable.ic_archive_black_24dp) {
      startActivity(Intent(this, CargosActivity::class.java))
    },
    MenuItem(R.string.separation, R.drawable.ic_shopping_cart_black_24dp),
    MenuItem(R.string.expedition, R.drawable.ic_directions_bus_black_24dp),
    MenuItem(R.string.inventory, R.drawable.ic_assignment_black_24dp),
    MenuItem(R.string.fillment, R.drawable.ic_format_color_fill_black_24dp),
    MenuItem(R.string.menu_exit, R.drawable.ic_exit_to_app_black_24dp) {
      exitAppHandler.logout()
    }
  )

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    menusList = findViewById(R.id.menu_list)
    menusList.adapter = MenusListAdapter(menus) { it.actionWhenClicked() }
    menusList.layoutManager = LinearLayoutManager(this)

    lifecycleScope.launchWhenCreated {
      delay(500L)
      globalData.userDetails?.let {
        updateHeaderUserDetailsUI(it)
      }
    }

    exitAppHandler = ExitAppHandler(this)
  }

  override fun onBackPressed() {
    exitAppHandler.confirmExit(this)
  }

  private fun updateHeaderUserDetailsUI(userDetails: UserDetails) {
    findViewById<TextView>(R.id.username)?.apply {
      text = getString(R.string.helloUser, userDetails.name)
    }
    findViewById<TextView>(R.id.userFunction)?.apply {
      text = userDetails.userFunction
    }

  }
}

class MenusListAdapter(
  private val menus: List<MenuItem>,
  private val onClickItem: (MenuItem) -> Unit
) : RecyclerView.Adapter<MenuItemViewHolder>() {
  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder {
    return MenuItemViewHolder(parent.inflateToViewHolder(R.layout.item_main_menu), onClickItem)
  }

  override fun getItemCount(): Int {
    return menus.size
  }

  override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) {
    holder.bind(menus[position])
  }
}

class MenuItemViewHolder(
  val view: View,
  val onClickItem: (MenuItem) -> Unit
) : RecyclerView.ViewHolder(view) {

  private var menuName: TextView = view.findViewById(R.id.menu_name)
  private var menuIcon: ImageView = view.findViewById(R.id.menu_icon)

  fun bind(menuItem: MenuItem) {
    view.setOnClickListener {
      onClickItem(menuItem)
    }
    menuName.text = view.resources.getString(menuItem.name)
    menuIcon.setImageResource(menuItem.icon)
  }

}
