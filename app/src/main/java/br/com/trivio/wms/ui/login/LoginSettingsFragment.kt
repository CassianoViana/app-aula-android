package br.com.trivio.wms.ui.login

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import br.com.trivio.wms.R

class LoginSettingsFragment : PreferenceFragmentCompat() {
  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    setPreferencesFromResource(R.xml.login_settings_preferences, rootKey)
  }
}
