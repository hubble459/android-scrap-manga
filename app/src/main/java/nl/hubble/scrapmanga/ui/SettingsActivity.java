package nl.hubble.scrapmanga.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import java.io.File;
import java.io.IOException;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.FileUtil;

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
                new Thread(() -> {
                    try {
                        Utils.Parse.resetQueries(requireContext());
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Query file has been reset!", Toast.LENGTH_SHORT).show());
                    } catch (IOException e) {
                        e.printStackTrace();
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
                    }
                }).start();
                return true;
            });

            addListener(R.string.dark_mode, preference -> {
                if (((SwitchPreference) preference).isChecked()) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            });

            addListener(R.string.download_database, preference -> {
                String pathIn = requireContext().getExternalFilesDir(null).getAbsolutePath() + File.separator + "databases" + File.separator + DatabaseHelper.DB_NAME;
                String pathOut = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "manga.db";
                FileUtil.Copy dt = new FileUtil.Copy(pathIn, pathOut, new FileUtil.DownloadListener() {
                    @Override
                    public void onFinish() {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Finished downloading!", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onError(Exception e) {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
                    }

                    @Override
                    public void progress(String file, int percent) {

                    }

                    @Override
                    public void cancelled() {
                        requireActivity().runOnUiThread(() -> Toast.makeText(requireContext(), "Cancelled!", Toast.LENGTH_SHORT).show());
                    }
                });
                dt.start();
                Toast.makeText(requireContext(), "Downloading...", Toast.LENGTH_SHORT).show();
                return true;
            });

            addListener(R.string.supported_websites, preference -> {
                Spinner spinner = new Spinner(getContext(), Spinner.MODE_DIALOG);
                spinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new MangaScraper(getContext()).getEngineNames()));
                spinner.performClick();
                return true;
            });

            addListener(R.string.run_query, preference -> {
                Intent intent = new Intent(requireContext(), QueryActivity.class);
                startActivity(intent);
                return true;
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