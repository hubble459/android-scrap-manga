package nl.hubble.scrapmanga.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import java.io.IOException;

import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.CustomActivity;

public class SettingsActivity extends CustomActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            addListener(R.string.reset_queries_key, preference -> {
                try {
                    Utils.Parse.resetQueries(requireContext());
                    Toast.makeText(getContext(), "Query file has been reset!", Toast.LENGTH_SHORT).show();
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return false;
            });
        }

        private void addListener(@StringRes int key, Preference.OnPreferenceClickListener listener) {
            Preference p = findPreference(getString(key));
            if (p != null) {
                p.setOnPreferenceClickListener(listener);
            }
        }
    }
}