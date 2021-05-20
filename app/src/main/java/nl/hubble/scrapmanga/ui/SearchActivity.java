package nl.hubble.scrapmanga.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.SearchAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.LoadManga;
import nl.hubble.scrapmanga.util.SearchManga;
import nl.hubble.scrapmanga.view.MangaDetailView;

import static nl.hubble.scrapmanga.ui.MainActivity.READING_KEY;
import static nl.hubble.scrapmanga.util.DatabaseHelper.arrayAsString;
import static nl.hubble.scrapmanga.util.DatabaseHelper.filled;

public class SearchActivity extends CustomActivity implements LoadManga.OnFinishedListener, SearchManga.OnFinishedListener {
    private EditText searchBar;
    private MangaScraper scraper;
    private String selectedHostname = "mangakakalot";
    private ListView list;
    private ProgressBar loading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        scraper = new MangaScraper(this);

        list = findViewById(R.id.list);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Manga manga = (Manga) parent.getItemAtPosition(position);
            openManga(manga);
        });

        loading = findViewById(R.id.loading);
        ArrayList<String> searchEngineNames = scraper.getSearchEnginesNames();

        Spinner spinner = findViewById(R.id.hostname_spinner);
        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, searchEngineNames));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedHostname = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        searchBar = findViewById(R.id.search_bar);
        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(null);
                return true;
            }
            return false;
        });
    }

    private void openManga(Manga manga) {
        loading(true);
        try {
            new LoadManga(this, manga.getHref(), this);
        } catch (MalformedURLException e) {
            error(e);
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
        runOnUiThread(() -> {
            loading(false);

            BottomSheetDialog bsd = new BottomSheetDialog(this);
            bsd.setContentView(R.layout.view_search_bottom_sheet);

            // Title
            TextView title = bsd.findViewById(R.id.title);
            if (title != null) {
                title.setText(manga.getTitle());
            }

            // Cover
            if (filled(manga.getCover())) {
                ImageView cover = bsd.findViewById(R.id.cover);
                if (cover != null) {
                    GlideUrl url = new GlideUrl(manga.getCover(), new LazyHeaders.Builder()
                            .addHeader("Referer", manga.getHref())
                            .build());
                    Glide.with(this)
                            .load(url)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.error)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .into(cover);
                }
            }

            // Details
            MangaDetailView details = bsd.findViewById(R.id.details);
            if (details != null) {
                details.setStatus(manga.isStatus() ? getString(R.string.ongoing) : getString(R.string.finished));
                if (!manga.getAuthors().isEmpty()) {
                    details.setAuthors(arrayAsString(manga.getAuthors()));
                }
                if (!manga.getAltTitles().isEmpty()) {
                    details.setAltTitles(arrayAsString(manga.getAltTitles()));
                }
                if (!manga.getGenres().isEmpty()) {
                    details.setGenres(arrayAsString(manga.getGenres()));
                }
                if (manga.getUpdated() > 0) {
                    details.setUpdated(Utils.Parse.toString(manga.getUpdated()));
                }
            }

            // Description
            TextView description = bsd.findViewById(R.id.description);
            if (description != null) {
                description.setText(manga.getDescription());
            }

            // Read Button
            Button read = bsd.findViewById(R.id.read);
            if (read != null) {
                read.setOnClickListener(v -> {
                    Reading reading = addReadingAndManga(manga);
                    Intent intent = new Intent(this, MangaActivity.class);
                    intent.putExtra(READING_KEY, reading);
                    startActivity(intent);
                });
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
        runOnUiThread(() -> Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());

    }
}
