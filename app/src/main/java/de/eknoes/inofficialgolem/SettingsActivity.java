package de.eknoes.inofficialgolem;


import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;

public class SettingsActivity extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        findPreference("abo_key").setEnabled(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_abo", false));
        findPreference("media_rss").setEnabled(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean("has_abo", false));

        findPreference("how_to_key").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("https://account.golem.de/subscription"));
                startActivity(webIntent);
                return true;
            }
        });

        findPreference("privacy").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("http://projekte.eknoes.de/datenschutz.html"));
                startActivity(webIntent);
                return true;
            }
        });

        findPreference("start_contact").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                /*Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                        "mailto","projekte@eknoes.de", null));
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inofficial golem.de Reader");
                startActivity(Intent.createChooser(emailIntent, getResources().getString(R.string.sendMail)));*/
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.setData(Uri.parse("https://github.com/eknoes/golem-android-reader/issues"));
                startActivity(webIntent);


                return true;
            }
        });
    }

    @Override
    public void onSharedPreferenceChanged(final SharedPreferences sharedPreferences, String key) {
        if (key.equals("has_abo")) {
            findPreference("abo_key").setEnabled(sharedPreferences.getBoolean(key, false));
            findPreference("media_rss").setEnabled(sharedPreferences.getBoolean(key, true));
        }
        //TODO: Check Abo Token
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
        View v = findViewById(R.id.has_abo);
        if (v != null) {
            v.invalidate();
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
