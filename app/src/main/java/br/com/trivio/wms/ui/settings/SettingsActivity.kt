package br.com.trivio.wms.ui.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import br.com.trivio.wms.MyAppCompatActivity
import br.com.trivio.wms.R

class SettingsActivity : MyAppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_settings)
    val settingsFragment = SettingsFragment()
    supportFragmentManager
      .beginTransaction()
      .replace(
        R.id.settings,
        settingsFragment
      )
      .commit()
  }

  class SettingsFragment : PreferenceFragmentCompat() {

    private var voiceEnabled: SwitchPreferenceCompat? = null
    private var barcodeInputsCameraOpen: SwitchPreferenceCompat? = null

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
      setPreferencesFromResource(R.xml.app_settings_preferences, rootKey)
      findPreferenceComponents()

      voiceEnabled?.setOnPreferenceChangeListener { _, value -> true }
      barcodeInputsCameraOpen?.setOnPreferenceChangeListener { _, value -> true }

    }

    private fun findPreferenceComponents() {
      preferenceScreen.apply {
        voiceEnabled = findPreference("voice_enabled")
        barcodeInputsCameraOpen = findPreference("barcode_input_camera_open")
      }

    }

  }
}
