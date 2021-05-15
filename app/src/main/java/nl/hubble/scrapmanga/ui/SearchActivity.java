package nl.hubble.scrapmanga.ui;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.type.BaseScraper;
import nl.hubble.scraper.type.QueryScraper;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.SearchAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.util.SearchManga;

public class SearchActivity extends CustomActivity {
    private EditText searchBar;
    private ArrayList<BaseScraper> searchEngines;
    private String selectedHostname = "mangakakalot";
    private ListView list;
    private ProgressBar loading;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        MangaScraper scraper = new MangaScraper(this);

        list = findViewById(R.id.list);
        loading = findViewById(R.id.loading);
        searchEngines = scraper.getSearchEngines();
        ArrayList<String> searchEngineNames = new ArrayList<>();
        for (BaseScraper searchEngine : searchEngines) {
            if (searchEngine instanceof QueryScraper) {
                ArrayList<String> searchableHostnames = ((QueryScraper) searchEngine).searchableHostnames();
                for (String shn : searchableHostnames) {
                    if (!searchEngineNames.contains(shn)) {
                        searchEngineNames.add(shn);
                    }
                }
            } else {
                for (String hostname : searchEngine.hostnames()) {
                    if (!searchEngineNames.contains(hostname)) {
                        searchEngineNames.add(hostname);
                    }
                }
            }
        }

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

    public void search(View view) {
        loading(true);
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            searchBar.clearFocus();
        }

        String query = searchBar.getText().toString();
        BaseScraper scraper = searchEngine();
        if (scraper != null) {
            new SearchManga(scraper, query, new SearchManga.OnFinishedListener() {
                @Override
                public void finished(List<Manga> manga) {
                    runOnUiThread(() -> display(manga));
                }

                @Override
                public void error(Exception e) {
                    runOnUiThread(() -> Toast.makeText(SearchActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                }
            });
        }
    }

    private void loading(boolean on) {
        loading.setVisibility(on ? View.VISIBLE : View.GONE);
    }

    private void display(List<Manga> results) {
        list.setAdapter(new SearchAdapter(this, results));
        loading(false);
    }

    private BaseScraper searchEngine() {
        for (BaseScraper searchEngine : searchEngines) {
            for (String hostname : searchEngine.hostnames()) {
                if (hostname.equals(selectedHostname)) {
                    return searchEngine;
                }
            }
        }
        return null;
    }
}
