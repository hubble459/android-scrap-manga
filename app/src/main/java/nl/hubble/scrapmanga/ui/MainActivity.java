package nl.hubble.scrapmanga.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ReadingAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.LoadManga;
import nl.hubble.scrapmanga.view.LoadDialog;

public class MainActivity extends CustomActivity {
    public static final String READING_KEY = "reading";
    private final List<Reading> readingList = new ArrayList<>();
    private ReadingAdapter adapter;
    private ListView list;
    private SearchView searchBar;
    private View dismiss, loading, emptyText;
    private SQLiteDatabase db;
    private Menu menu;
    private boolean isLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initComponents();
    }

    private void initComponents() {
        searchBar = findViewById(R.id.search_bar);
        list = findViewById(R.id.list);
        list.setTextFilterEnabled(true);

        emptyText = findViewById(R.id.empty_text);

        DatabaseHelper dbh = new DatabaseHelper(this);
        db = dbh.getReadableDatabase();
        reloadReading();

        adapter = new ReadingAdapter(this, readingList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Reading r = (Reading) parent.getItemAtPosition(position);
            openManga(r);
        });

        list.setOnItemLongClickListener((parent, view, position, id) -> {
            Reading r = (Reading) parent.getItemAtPosition(position);
            deleteManga(r);
            return true;
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    list.clearTextFilter();
                } else {
                    list.setFilterText(newText);
                }
                return true;
            }
        });

        loading = findViewById(R.id.loading);
        dismiss = findViewById(R.id.dismiss);
    }

    private void deleteManga(Reading reading) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Manga")
                .setMessage(String.format("Are you sure you want to remove '%s' ?", reading.getTitle()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    load(true);
                    DatabaseHelper.reset(db, reading);
                    reloadReading();
                    Toast.makeText(this, String.format("Removed '%s'", reading.getTitle()), Toast.LENGTH_SHORT).show();
                    load(false);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void reloadReading() {
        readingList.clear();
        readingList.addAll(DatabaseHelper.getAllReading(db));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            adapter.notifyFilter(readingList);
        }
        emptyText.setVisibility(readingList.isEmpty() ? View.VISIBLE : View.GONE);
        list.setFilterText(searchBar.getQuery().toString());
    }

    private void exportToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < readingList.size(); i++) {
                Reading reading = readingList.get(i);
                sb.append(reading.getHref());
                if (i < readingList.size() - 1) {
                    sb.append('\n');
                }
            }

            ClipData clip = ClipData.newPlainText("manga_list", sb.toString());
            clipboard.setPrimaryClip(clip);
        }
    }

    private boolean isNightModeEnabled() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPref.getBoolean(getString(R.string.dark_mode), false);
    }

    public void toggleNightMode(MenuItem item) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getString(R.string.dark_mode), !item.isChecked());
        editor.apply();

        item.setChecked(!item.isChecked());
        if (item.isChecked()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void addManga(MenuItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText input = new EditText(this);

        input.setHint("Manga URL");
        input.setSelectAllOnFocus(true);
        layout.addView(input);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            layout.setPaddingRelative(32, 32, 32, 32);
        } else {
            layout.setPadding(32, 32, 32, 32);
        }

        // Fill input with copied url
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = clipboard.getPrimaryClip();
            if (clip != null && clip.getItemCount() > 0) {
                CharSequence text = clip.getItemAt(0).getText();
                if (isMultiURL(text.toString())) {
                    input.setText(text);
                }
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.add_manga)
                .setView(layout)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton("Add", (dialog, which) -> {
                    String inputText = input.getText().toString();
                    load(true);
                    if (isURL(inputText)) {
                        loadManga(inputText);
                    } else if (isMultiURL(inputText)) {
                        tryMultipleManga(inputText);
                    }

                })
                .show();
    }


    private void tryMultipleManga(String inputText) {
        String[] urls = inputText.split("[\\n]");

        if (urls.length <= 1) {
            Toast.makeText(this, "Not a valid URL!", Toast.LENGTH_SHORT).show();
            load(false);
        } else {
            loadMultipleManga(urls);
        }
    }

    private void loadMultipleManga(String[] urls_) {
        List<String> urls = new ArrayList<>();
        for (String url : urls_) {
            if (!url.isEmpty()) {
                urls.add(url);
            }
        }

        List<Thread> threads = new ArrayList<>();

        LoadDialog ld = new LoadDialog(this,
                urls.size(),
                // Cancel
                dialog -> {
                    for (Thread thread : threads) {
                        thread.interrupt();
                    }
                },
                // Retry
                errors -> {
                    List<String> urlList = new ArrayList<>();
                    for (LoadDialog.Error error : errors) {
                        urlList.add((String) error.getTag());
                    }
                    load(true);
                    loadMultipleManga(urlList.toArray(new String[0]));
                });

        for (String url : urls) {
            if (!DatabaseHelper.exists(db, url)) {
                try {
                    URL urlObj = new URL(url);
                    threads.add(new Thread(() -> {
                        try {
                            MangaScraper ms = new MangaScraper(this);
                            Manga m = ms.parse(urlObj);
                            addReadingAndManga(m);
                        } catch (Exception e) {
                            runOnUiThread(() -> ld.addError(url, e.getMessage(), url));
                        }
                        runOnUiThread(ld::increaseCount);
                    }));
                } catch (MalformedURLException e) {
                    ld.addError(url, getString(R.string.invalid_url), url);
                }
            } else {
                ld.addError(url, getString(R.string.already_exists), url);
                ld.increaseCount();
            }
        }

        for (Thread t : threads) {
            t.start();
        }
        new Thread(() -> {
            for (Thread t : threads) {
                try {
                    if (!t.isInterrupted())
                        t.join();
                } catch (InterruptedException ignored) {
                }
            }

            runOnUiThread(() -> {
                ld.done();
                reloadReading();
                load(false);
            });
        }).start();
    }

    private void loadManga(String url) {
        if (DatabaseHelper.exists(db, url)) {
            Toast.makeText(this, "This manga already exists!", Toast.LENGTH_SHORT).show();
            load(false);
            return;
        }

        try {
            new LoadManga(this, url, new LoadManga.OnFinishedListener() {
                @Override
                public void finished(Manga manga) {
                    Reading reading = addReadingAndManga(manga);

                    runOnUiThread(() -> {
                        openManga(reading);
                        load(false);
                    });
                }

                @Override
                public void error(Exception e) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        load(false);
                    });
                }
            });
        } catch (MalformedURLException e) {
            Toast.makeText(this, "Not a valid URL!", Toast.LENGTH_SHORT).show();
            load(false);
        }
    }

    private synchronized Reading addReadingAndManga(Manga manga) {
        Reading reading = new Reading();

        reading.setTotalChapters(manga.getChapters().size());
        reading.setTitle(manga.getTitle());
        reading.setHref(manga.getHref());
        reading.setHostname(manga.getHostname());
        reading.setAutoRefresh(true);

        DatabaseHelper.insertReading(db, reading);
        DatabaseHelper.insertManga(db, reading, manga);
        return reading;
    }

    private void openManga(Reading reading) {
        Intent intent = new Intent(this, MangaActivity.class);
        intent.putExtra(READING_KEY, reading);
        startActivity(intent);
    }

    private void load(boolean start) {
        isLoading = start;
        if (start) {
            menu.setGroupVisible(0, false);
            loading.setVisibility(View.VISIBLE);
            dismiss.setVisibility(View.VISIBLE);
        } else {
            menu.setGroupVisible(0, true);
            loading.setVisibility(View.GONE);
            dismiss.setVisibility(View.GONE);
        }
    }

    private boolean isURL(CharSequence text) {
        return Patterns.WEB_URL.matcher(text).matches();
    }

    private boolean isMultiURL(String text) {
        return text.matches("(http(s)?://(.+\\.)+.{2,}\\n?)+");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem darkModeToggle = menu.findItem(R.id.dark_mode);
        darkModeToggle.setChecked(isNightModeEnabled());
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (db != null) {
            reloadReading();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return isLoading;
    }

    public void refresh(MenuItem item) {
        load(true);

        refresh(adapter.getList());
    }

    private void refresh(List<Reading> readingList) {
        List<Thread> threads = new ArrayList<>();

        LoadDialog ld = new LoadDialog(this,
                readingList.size(),
                // Cancel
                dialog -> {
                    for (Thread thread : threads) {
                        thread.interrupt();
                    }
                },
                // Retry
                errors -> {
                    List<Reading> urlList = new ArrayList<>();
                    for (LoadDialog.Error error : errors) {
                        urlList.add((Reading) error.getTag());
                    }
                    refresh(urlList);
                });


        long now = System.currentTimeMillis();
        for (Reading reading : readingList) {
            if (reading.getTotalChapters() - reading.getChapter() <= 3 && now - reading.getRefreshed() > 10 * 60 * 1000 /*10 min*/) {
                Thread t = new Thread(() -> {
                    try {
                        MangaScraper ms = new MangaScraper(this);
                        Manga manga = ms.parse(new URL(reading.getHref()));
                        if (!isComplete(manga)) {
                            throw new Exception("Manga is missing important data");
                        } else {
                            update(reading, manga);
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> ld.addError(reading.getTitle(), e.getMessage(), reading));
                    }
                    runOnUiThread(ld::increaseCount);
                });
                t.start();
                threads.add(t);
            } else {
                ld.increaseCount();
            }
        }

        new Thread(() -> {
            for (Thread thread : threads) {
                try {
                    if (!thread.isInterrupted()) {
                        thread.join();
                    }
                } catch (InterruptedException ignored) {
                }
            }

            runOnUiThread(() -> {
                ld.done();
                load(false);
            });
        }).start();
    }

    private boolean isComplete(Manga manga) {
        return filled(manga.getTitle()) && filled(manga.getHref()) && filled(manga.getHostname());
    }

    private boolean filled(Object object) {
        boolean res = false;
        if (object != null) {
            res = true;
            if (object instanceof String) {
                res = !((String) object).isEmpty();
            } else if (object instanceof Integer) {
                res = ((Integer) object) != -1;
            }
        }
        return res;
    }

    private synchronized void update(Reading reading, Manga manga) {
        ContentValues cv = new ContentValues();
        cv.put("title", manga.getTitle());
        cv.put("href", manga.getHref());
        cv.put("total_chapters", manga.getChapters().size());

        DatabaseHelper.updateManga(db, manga, reading);

        db.update("reading", cv, "href IS ?", new String[]{reading.getHref()});
    }

    public void settings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}