package nl.hubble.scrapmanga.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.SearchAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.ImageUtil;
import nl.hubble.scrapmanga.util.LoadManga;
import nl.hubble.scrapmanga.util.SearchManga;
import nl.hubble.scrapmanga.view.MangaDetailView;

import static nl.hubble.scrapmanga.ui.MainActivity.READING_KEY;
import static nl.hubble.scrapmanga.util.DatabaseHelper.arrayAsString;
import static nl.hubble.scrapmanga.util.DatabaseHelper.notEmpty;

public class SearchActivity extends CustomActivity implements LoadManga.OnFinishedListener, SearchManga.OnFinishedListener {
    private EditText searchBar;
    private MangaScraper scraper;
    private String selectedHostname;
    private ListView list;
    private ProgressBar loading;
    private boolean destroyed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        destroyed = false;
        scraper = new MangaScraper(this);

        list = findViewById(R.id.list);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Manga manga = (Manga) parent.getItemAtPosition(position);
            openManga(manga);
        });

        findViewById(R.id.search_button).setOnClickListener(this::search);

        loading = findViewById(R.id.loading);
        ArrayList<String> searchEngineNames = scraper.getSearchEnginesNames();
        Collections.sort(searchEngineNames);
        searchEngineNames.add(0, "all");

        Spinner spinner = findViewById(R.id.hostname_spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchEngineNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                setPreviousSelection(position);
                selectedHostname = (String) parent.getItemAtPosition(position);
                if (selectedHostname.equals("all")) {
                    selectedHostname = null;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setSelection(getPreviousSelection());

        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(null);
                return true;
            }
            return false;
        });
    }

    private int getPreviousSelection() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getInt(getString(R.string.search_selection), 0);
    }

    private void setPreviousSelection(int selection) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(getString(R.string.search_selection), selection);
        editor.apply();
    }

    private void openManga(Manga manga) {
        int readingId = DatabaseHelper.exists(this, manga.getHref());
        if (readingId != -1) {
            Reading reading = DatabaseHelper.getReading(this, readingId);
            manga = DatabaseHelper.getManga(this, reading);
            finished(manga, reading);
        } else {
            loading(true);
            try {
                new LoadManga(this, manga.getHref(), this);
            } catch (MalformedURLException e) {
                error(e);
            }
        }
    }

    public void search(View view) {
        loading(true);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            searchBar.clearFocus();
        }

        String query = searchBar.getText().toString();
        new SearchManga(scraper, selectedHostname, query, this);
    }

    private void loading(boolean on) {
        loading.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void display(List<Manga> results) {
        list.setAdapter(new SearchAdapter(this, results));
        loading(false);
    }

    @Override
    public void finished(Manga manga) {
        finished(manga, null);
    }

    @Override
    protected void onDestroy() {
        this.destroyed = true;
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        this.destroyed = true;
        super.onPause();
    }

    @Override
    protected void onResume() {
        this.destroyed = false;
        super.onResume();
    }

    @Override
    protected void onRestart() {
        this.destroyed = false;
        super.onRestart();
    }

    public void finished(Manga manga, final Reading rding) {
        if (destroyed) {
            return;
        }
        runOnUiThread(() -> {
            if (destroyed) {
                return;
            }

            loading(false);
            Reading[] reading = new Reading[]{rding};

            BottomSheetDialog bsd = new BottomSheetDialog(this);
            bsd.setContentView(R.layout.view_search_bottom_sheet);

            // Title
            TextView title = bsd.findViewById(R.id.title);
            if (title != null) {
                title.setText(manga.getTitle());
            }

            // Cover
            ImageView cover = bsd.findViewById(R.id.cover);
            if (cover != null) {
                boolean notEmpty = notEmpty(manga.getCover());
                cover.setVisibility(notEmpty ? View.VISIBLE : View.GONE);
                if (notEmpty) {
                    ImageUtil.loadImage(cover, manga.getCover(), null, manga.getHref(), false);
                }
            }

            // Details
            MangaDetailView details = bsd.findViewById(R.id.details);
            if (details != null) {
                details.setStatus(manga.getStatus() ? getString(R.string.ongoing) : getString(R.string.finished));
                List<String> authors = manga.getAuthors();
                if (authors != null && !authors.isEmpty()) {
                    details.setAuthors(arrayAsString(authors.subList(0, Math.min(authors.size(), 3))));
                }
                List<String> altTitles = manga.getAltTitles();
                if (altTitles != null && !altTitles.isEmpty()) {
                    details.setAltTitles(arrayAsString(altTitles.subList(0, Math.min(altTitles.size(), 3))));
                }
                if (!manga.getGenres().isEmpty()) {
                    details.setGenres(arrayAsString(manga.getGenres()));
                }
                if (manga.getInterval() != null) {
                    details.setInterval(manga.getInterval());
                }
                if (manga.getUpdated() > 0) {
                    details.setUpdated(Utils.Parse.toTimeString(manga.getUpdated()));
                }
                if (manga.getChapters() != null) {
                    details.setChapters(manga.getChapters().size());
                }
            }

            // Description
            TextView description = bsd.findViewById(R.id.description);
            if (description != null) {
                String desc = manga.getDescription();
                if (notEmpty(desc)) {
                    description.setText(String.format(Locale.getDefault(), "%s%s", desc.substring(0, Math.min(500, desc.length())), desc.length() > 500 ? "..." : ""));
                } else {
                    description.setVisibility(View.GONE);
                }
            }

            // Read Button
            Button read = bsd.findViewById(R.id.read);
            if (read != null) {
                if (reading[0] != null) {
                    read.setText(R.string.open);
                }
                read.setOnClickListener(v -> {
                    if (reading[0] == null) {
                        reading[0] = addReadingAndManga(manga);
                    }
                    bsd.dismiss();
                    Intent intent = new Intent(this, MangaActivity.class);
                    intent.putExtra(READING_KEY, reading[0]);
                    startActivity(intent);
                    finish();
                });

                // Add Button
                Button add = bsd.findViewById(R.id.add);
                if (add != null) {
                    if (reading[0] != null) {
                        add.setVisibility(View.GONE);
                    } else {
                        add.setOnClickListener(v -> {
                            reading[0] = addReadingAndManga(manga);
                            add.setVisibility(View.GONE);
                            read.setText(R.string.open);
                        });
                    }
                }
            }

            bsd.show();
        });
    }

    private Reading addReadingAndManga(Manga manga) {
        Reading reading = new Reading();

        reading.setTotalChapters(manga.getChapters().size());
        reading.setTitle(manga.getTitle());
        reading.setHref(manga.getHref());
        reading.setHostname(manga.getHostname());
        reading.setAutoRefresh(true);

        DatabaseHelper.insertReading(this, reading);
        DatabaseHelper.insertManga(this, reading, manga);
        return reading;
    }

    @Override
    public void finished(List<Manga> manga) {
        runOnUiThread(() -> display(manga));
    }

    @Override
    public void error(Exception e) {
        runOnUiThread(() -> {
            Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            loading(false);
        });

    }
}
