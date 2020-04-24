package br.com.trivio.wms.ui.login

import android.os.Bundle
import android.view.MenuItem
import android.webkit.URLUtil
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.CheckBoxPreference
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import br.com.trivio.wms.R
import br.com.trivio.wms.handleHomeClickFinish
import br.com.trivio.wms.setupToolbar

class LoginSettingsActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.settings_activity)
    val settingsFragment = SettingsFragment()
    settingsFragment.validateUrls()
    supportFragmentManager
      .beginTransaction()
      .replace(
        R.id.settings,
        settingsFragment
      )
      .commit()
    setupToolbar()
  }

  override fun onOptionsItemSelected(item: MenuItem?): Boolean {
    this.handleHomeClickFinish(item)
    return super.onOptionsItemSelected(item)
  }

  class SettingsFragment : PreferenceFragmentCompat() {

    private var developerMode: CheckBoxPreference? = null
    private var serverUrl: EditTextPreference? = null
    private var localUrl: EditTextPreference? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.login_settings_preferences, rootKey)
      findPreferenceComponents()
      validateUrls()
      updateDevModeStatus(developerMode?.isChecked)

      developerMode?.setOnPreferenceChangeListener { _, value ->
        updateDevModeStatus(value as Boolean?)
        true
      }

      serverUrl?.setOnPreferenceChangeListener { _, value ->
        !isNotValidUrl(value as String)
      }

      localUrl?.setOnPreferenceChangeListener { _, value ->
        !isNotValidUrl(value as String)
      }
    }

    private fun findPreferenceComponents() {
      localUrl = preferenceScreen.findPreference("local_url")
      serverUrl = preferenceScreen.findPreference("server_url")
      developerMode = findPreference<CheckBoxPreference>("developer_mode")
    }

    private fun validateUrl(
      editPrefText: EditTextPreference?,
      key: String,
      defaultUrl: String = "https://api.wms.trivio.com.br"
    ) {
      editPrefText?.let {
        val url = it.text
        if (isNotValidUrl(url)) {
          preferenceManager.sharedPreferences.edit()
            .putString(key, defaultUrl).apply()
        }
      }
    }

    private fun isNotValidUrl(url: String?) = !URLUtil.isValidUrl(url)

    fun validateUrls() {
      validateUrl(localUrl, "local_url", "http://192.168.15.x.x")
      validateUrl(serverUrl, "server_url")
    }

    private fun updateDevModeStatus(isDevModeChecked: Boolean?) {
      isDevModeChecked?.let {
        serverUrl?.isEnabled = !it
        localUrl?.isEnabled = it
      }
    }
  }
}
