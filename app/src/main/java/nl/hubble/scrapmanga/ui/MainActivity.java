package nl.hubble.scrapmanga.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nl.hubble.scraper.MangaScraper;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ReadingAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.FileUtil;
import nl.hubble.scrapmanga.util.LoadManga;
import nl.hubble.scrapmanga.view.LoadDialog;

public class MainActivity extends CustomActivity {
    public static final String READING_KEY = "reading";
    private final List<Reading> readingList = new ArrayList<>();
    private ReadingAdapter adapter;
    private ListView list;
    private SearchView searchBar;
    private View dismiss, loading, emptyText;
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

//       if (exportOldDb()) return;

        reloadReading();

        adapter = new ReadingAdapter(this, readingList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            Reading r = (Reading) parent.getItemAtPosition(position);
            openManga(r);
        });

        list.setOnItemLongClickListener((parent, view, position, id) -> {
            Reading r = (Reading) parent.getItemAtPosition(position);
            confirmRemoveManga(r);
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

    private boolean exportOldDb() {
        String oldDbPath = Objects.requireNonNull(getFilesDir().getParentFile()).getAbsolutePath() + File.separator + "databases" + File.separator + "manga.db";
        Toast.makeText(this, oldDbPath, Toast.LENGTH_SHORT).show();
        if (FileUtil.exists(oldDbPath)) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("Old Database found...")
                    .setMessage("\uD83D\uDE33")
                    .setPositiveButton("SEND TO BLUE MAN", (dialog, which) -> {
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);
                        emailIntent.setType("vnd.android.cursor.dir/email");
                        String[] to = {"geraldd459@gmail.com"};
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
                        emailIntent.putExtra(Intent.EXTRA_STREAM, oldDbPath);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Database");
                        startActivity(Intent.createChooser(emailIntent, "Send email..."));
                    })
                    .setNeutralButton("Download", (dialog, which) -> FileUtil.copy(oldDbPath, getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "manga.db", new FileUtil.DownloadListener() {
                        @Override
                        public void onFinish() {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, "Downloaded!", Toast.LENGTH_SHORT).show());
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                        }

                        @Override
                        public void progress(String file, int percent) {

                        }

                        @Override
                        public void cancelled() {

                        }
                    }))
                    .show();
            return true;
        }
        return false;
    }

    private void confirmRemoveManga(Reading reading) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_manga)
                .setMessage(String.format(getString(R.string.delete_manga_confirm), reading.getTitle()))
                .setPositiveButton(R.string.yes, (dialog, which) -> {
                    load(true);
                    removeManga(reading);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void removeManga(Reading reading) {
        new Thread(() -> {
            DatabaseHelper.remove(this, reading);

            runOnUiThread(() -> {
                reloadReading();
                Toast.makeText(this, String.format(getString(R.string.removed_manga), reading.getTitle()), Toast.LENGTH_SHORT).show();
                load(false);
            });
        }).start();
    }

    private void reloadReading() {
        readingList.clear();
        readingList.addAll(DatabaseHelper.getAllReading(this));
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

    public void addManga(MenuItem item) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        EditText input = new EditText(this);

        input.setHint(R.string.manga_url);
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
                .setPositiveButton(R.string.add, (dialog, which) -> {
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
            Toast.makeText(this, R.string.invalid_url, Toast.LENGTH_SHORT).show();
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
            if (DatabaseHelper.exists(this, url) == -1) {
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
        if (DatabaseHelper.exists(this, url) != -1) {
            Toast.makeText(this, getString(R.string.manga_already_exists), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(this, getString(R.string.invalid_url), Toast.LENGTH_SHORT).show();
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

        DatabaseHelper.insertReading(this, reading);
        DatabaseHelper.insertManga(this, reading, manga);
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
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (list != null) {
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
        boolean[] canceled = new boolean[1];
        LoadDialog ld = new LoadDialog(this,
                readingList.size(),
                // Cancel
                dialog -> canceled[0] = true,
                // Retry
                errors -> {
                    List<Reading> urlList = new ArrayList<>();
                    for (LoadDialog.Error error : errors) {
                        urlList.add((Reading) error.getTag());
                    }
                    refresh(urlList);
                });

        new Thread(() -> {
            MangaScraper ms = new MangaScraper(this);
            long now = System.currentTimeMillis();
            for (Reading reading : readingList) {
                if (!canceled[0] && reading.getTotalChapters() - reading.getChapter() <= 3 && now - reading.getRefreshed() >= 600000 /*10 min*/) {
                    try {
                        runOnUiThread(() -> ld.setCurrentTask(reading.getTitle()));
                        Manga manga = ms.parse(new URL(reading.getHref()));
                        if (!isComplete(manga)) {
                            throw new Exception(getString(R.string.manga_missing_data));
                        } else {
                            update(reading, manga);
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> ld.addError(reading.getTitle(), e.getMessage(), reading));
                    }
                }
                runOnUiThread(ld::increaseCount);
            }
            runOnUiThread(() -> {
                ld.done();
                load(false);
                reloadReading();
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
        int totalChapters = manga.getChapters().size();

        ContentValues cv = new ContentValues();
        cv.put("title", manga.getTitle());
        cv.put("href", manga.getHref());
        cv.put("total_chapters", totalChapters);

        reading.setTitle(manga.getTitle());
        reading.setHref(manga.getHref());
        reading.setTotalChapters(totalChapters);
        reading.setRefreshed(System.currentTimeMillis());

        DatabaseHelper.updateManga(this, manga, reading.getId());
        DatabaseHelper.updateReading(this, reading);
    }

    public void settings(MenuItem item) {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    public void searchOnline(MenuItem item) {
        Intent intent = new Intent(this, SearchActivity.class);
        startActivity(intent);
    }

    public void statistics(MenuItem item) {
        Intent intent = new Intent(this, StatsActivity.class);
        startActivity(intent);
    }
}