package de.eknoes.inofficialgolem

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import de.eknoes.inofficialgolem.ArticleFragment.*

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.preferences, rootKey)

            val aboKey = findPreference<EditTextPreference>("abo_key")

            aboKey?.setOnPreferenceChangeListener { _: Preference, _: Any ->
                PreferenceManager.getDefaultSharedPreferences(context).edit() .putLong("last_refresh", 0).apply()
                true
            }
        }

        override fun onPreferenceTreeClick(preference: Preference?): Boolean {
            if (preference != null) {
                if (preference.key == "choose_layout") {
                    val intent = Intent(context, ArticleView::class.java)
                    intent.putExtra(ARTICLE_URL, "https://www.golem.de/sonstiges/ansicht/")
                    intent.putExtra(FORCE_WEBVIEW, true)
                    intent.putExtra(NO_ARTICLE, true)
                    startActivity(intent)
                    return true
                } else if (preference.key == "how_to_key") {
                    val intent = Intent(context, ArticleView::class.java)
                    intent.putExtra(ARTICLE_URL, "https://account.golem.de/product/subscription")
                    intent.putExtra(FORCE_WEBVIEW, true)
                    intent.putExtra(NO_ARTICLE, true)
                    startActivity(intent)
                    return true
                } else if (preference.key == "how_to_darkmode") {
                    val intent = Intent(context, ArticleView::class.java)
                    intent.putExtra(ARTICLE_URL, "https://account.golem.de/product/subscription#videotype1")
                    intent.putExtra(FORCE_WEBVIEW, true)
                    intent.putExtra(NO_ARTICLE, true)
                    startActivity(intent)
                    return true
                } else if (preference.key == "start_contact") {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://github.com/eknoes/golem-android-reader/issues")
                    startActivity(intent)
                    return true
                } else if (preference.key == "join_beta") {
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://play.google.com/apps/testing/de.eknoes.inofficialgolem")
                    startActivity(intent)
                    return true
                } else if (preference.key == "privacy") {
                    val intent = Intent(context, ArticleView::class.java)
                    intent.putExtra(ARTICLE_URL, "https://projekte.eknoes.de/datenschutz.html")
                    intent.putExtra(FORCE_WEBVIEW, true)
                    intent.putExtra(NO_ARTICLE, true)
                    startActivity(intent)
                    return true
                }

            }

            return super.onPreferenceTreeClick(preference)
        }
        
    }
}