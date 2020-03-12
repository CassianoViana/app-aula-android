package br.com.trivio.wms.data

import android.content.Context
import android.preference.PreferenceManager
import br.com.trivio.wms.data.model.UserDetails

class GlobalData {

  lateinit var userDetails: UserDetails
  lateinit var appContext: Context

  var token: String?
    set(token) {
      editor(appContext).putString("token", token).commit()
    }
    get(): String? {
      return sharedPreferences(appContext).getString("token", null)
    }

  private fun editor(context: Context) =
    sharedPreferences(context).edit()

  private fun sharedPreferences(context: Context) =
    PreferenceManager.getDefaultSharedPreferences(context)
}
