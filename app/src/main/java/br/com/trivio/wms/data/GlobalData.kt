package br.com.trivio.wms.data

import android.content.Context
import android.preference.PreferenceManager
import br.com.trivio.wms.data.model.UserDetails

class GlobalData {

  var userDetails: UserDetails? = null
    set(value) {
      value?.let {
        editor().putLong("userDetails_id", value.id).commit()
        editor().putLong("userDetails_ownerId", value.ownerId).commit()
        editor().putString("userDetails_name", value.name).commit()
        editor().putString("userDetails_username", value.username).commit()
      }
      field = value
    }
  get() {
    if(field == null){
      val prefs = sharedPreferences()
      field = UserDetails(
        id = prefs.getLong("userDetails_id", 0),
        ownerId = prefs.getLong("userDetails_ownerId", 0),
        name = prefs.getString("userDetails_name", "")!!,
        username = prefs.getString("userDetails_username", "")!!
      )
    }
    return field
  }
  lateinit var appContext: Context

  var token: String?
    set(token) {
      editor().putString("token", token).commit()
    }
    get(): String? {
      return sharedPreferences().getString("token", null)
    }

  private fun editor() =
    sharedPreferences().edit()

  private fun sharedPreferences() =
    PreferenceManager.getDefaultSharedPreferences(appContext)
}
