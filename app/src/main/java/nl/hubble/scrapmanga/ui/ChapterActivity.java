package nl.hubble.scrapmanga.ui;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.net.MalformedURLException;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.LoadManga;
import nl.hubble.scrapmanga.view.MangaDetailView;

public class ChapterActivity extends CustomActivity implements LoadManga.OnFinishedListener {
    public static final String MANGA_KEY = "manga";
    private SQLiteDatabase db;
    private Reading reading;
    private Manga manga;
    private TextView number;
    private ProgressBar readingProgress;
    private Button retry;
    private ProgressBar loading;
    private TextView errorText;
    private long refreshTime = (long) 8.64e+7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter);

        reading = (Reading) getIntent().getSerializableExtra(MainActivity.READING_KEY);
        retry = findViewById(R.id.retry);
        loading = findViewById(R.id.loading);
        errorText = findViewById(R.id.error);

        if (reading == null) {
            error(new Exception(getString(R.string.manga_non_existent)));
        } else {
            getRefreshTime();
            init(false);
        }
    }

    private void getRefreshTime() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String longString = sharedPref.getString(getString(R.string.auto_refresh_time), "8.64E7");
        double d = Double.parseDouble(longString);
        refreshTime = (long) d;
    }

    private void init(boolean force) {
        new Thread(() -> {
            if (db == null) {
                db = DatabaseHelper.getDatabase(this);
            }

            manga = DatabaseHelper.getManga(db, reading);
            if (manga == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "manga is null ówò?", Toast.LENGTH_SHORT).show();
                    stopLoading();
                });
            } else {
                loadManga(force);
            }
        }).start();
    }

    private void updateReading(int value) {
        if (reading.chapter != value) {
            reading.chapter = value;
            DatabaseHelper.updateReading(db, reading);
        }

        if (readingProgress != null && readingProgress.getProgress() != value) {
            readingProgress.setProgress(value);
        }
        if (number != null) {
            number.setText(String.valueOf(value));
        }
    }

    private void loadManga(boolean force) {
        if ((reading.autoRefresh && System.currentTimeMillis() - reading.refreshed > refreshTime) || force) {
            try {
                new LoadManga(this, reading.href, this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            reading = DatabaseHelper.getReading(db, reading.id);
            manga = DatabaseHelper.getManga(db, reading);
            finished(manga);
        }
    }

    private boolean isComplete(Manga manga) {
        return filled(manga.title) && filled(manga.href) && filled(manga.hostname);
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

    @Override
    public void finished(Manga m) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1 && isDestroyed() || isFinishing() || m == null) {
            return;
        }

        runOnUiThread(() -> {
            if (!isComplete(m)) {
                Toast.makeText(this, "Manga is missing important data", Toast.LENGTH_SHORT).show();
                stopLoading();
                return;
            }

            if (manga != m) {
                m.id = manga.id;
                manga = m;
                reading.href = m.href;
                reading.title = m.title;
                reading.totalChapters = m.chapters.size();
                reading.hostname = m.hostname;
                reading.refreshed = System.currentTimeMillis();
                DatabaseHelper.updateManga(db, manga);
            }
            DatabaseHelper.updateReading(db, reading);

            // Title
            TextView title = findViewById(R.id.title);
            title.setText(manga.title);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setSubtitle(manga.hostname);
            }

            // Cover
            if (filled(manga.cover)) {
                ImageView cover = findViewById(R.id.cover);
                GlideUrl url = new GlideUrl(manga.cover, new LazyHeaders.Builder()
                        .addHeader("Referer", manga.href)
                        .build());
                Glide.with(this)
                        .load(url)
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(cover);
            }

            // Details
            MangaDetailView details = findViewById(R.id.details);
            details.setStatus(manga.status ? getString(R.string.ongoing) : getString(R.string.finished));
            if (!manga.authors.isEmpty()) {
                details.setAuthors(arrayAsString(manga.authors));
            }
            if (!manga.altTitles.isEmpty()) {
                details.setAltTitles(arrayAsString(manga.altTitles));
            }
            if (!manga.genres.isEmpty()) {
                details.setGenres(arrayAsString(manga.genres));
            }
            if (manga.updated > 0) {
                details.setUpdated(Utils.Parse.toString(manga.updated));
            }

            // Description
            findViewById(R.id.desc_title).setVisibility(View.VISIBLE);
            TextView description = findViewById(R.id.description);
            description.setText(manga.description);

            // Seekbar and Number
            TextView total = findViewById(R.id.total_chapters);
            total.setText(String.valueOf(reading.totalChapters));
            number = findViewById(R.id.current_chapter);
            readingProgress = findViewById(R.id.current_chapter_progress);

            number.setText(String.valueOf(reading.chapter));
            readingProgress.setMax(reading.totalChapters);
            readingProgress.setProgress(reading.chapter);

            findViewById(R.id.bottom_panel).setVisibility(View.VISIBLE);

            stopLoading();
        });
    }

    private void stopLoading() {
        loading.setVisibility(View.GONE);
        errorText.setText("");
    }

    private String arrayAsString(List<?> list) {
        return list.toString().replaceAll("[\\[\\]]", "").replace(",", ";");
    }

    @Override
    public void error(Exception e) {
        runOnUiThread(() -> {
            loading.setVisibility(View.GONE);
            if (reading != null) {
                retry.setVisibility(View.VISIBLE);
            }
            errorText.setTextColor(Color.RED);
            errorText.setText(e.getMessage());
        });
    }

    public void changeCurrent(View view) {
        NumberPicker np = new NumberPicker(this);
        np.setMaxValue(reading.totalChapters);
        np.setValue(reading.chapter);

        new AlertDialog.Builder(this)
                .setTitle(R.string.chapter)
                .setView(np)
                .setPositiveButton(R.string.save, (dialog, which) -> updateReading(np.getValue()))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void showChapters(View view) {
        Intent intent = new Intent(this, ChapterListActivity.class);
        intent.putExtra(MANGA_KEY, manga);
        intent.putExtra(MainActivity.READING_KEY, reading);
        startActivity(intent);
    }

    public void continueReading(View view) {
        int chap = Math.min(reading.totalChapters - 1, reading.totalChapters - reading.chapter);

        read(chap);
    }

    public void readFirst(View view) {
        read(reading.totalChapters - 1);
    }

    public void readLast(View view) {
        if (reading.chapter < reading.totalChapters - 1) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.warning)
                    .setMessage(R.string.skipping_chapter_warning)
                    .setPositiveButton(R.string.yes, (dialog, which) -> read(0))
                    .setNegativeButton(R.string.cancel, null)
                    .show();
        } else {
            read(0);
        }
    }

    private void read(int chap) {
        List<Chapter> chapters = manga.chapters;
        if (!chapters.isEmpty()) {
            updateReading(reading.totalChapters - chap);

            Chapter chapter = manga.chapters.get(chap);
            Intent intent = new Intent(this, ReadActivity.class);
            intent.putExtra(ChapterListActivity.CHAPTER_KEY, chapter);
            intent.putExtra(ChapterActivity.MANGA_KEY, manga);
            intent.putExtra(MainActivity.READING_KEY, reading);
            startActivity(intent);
        } else {
            Toast.makeText(this, getString(R.string.there_are_no_chapters), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (readingProgress != null && reading != null && number != null && db != null) {
            Reading reading = DatabaseHelper.getReading(db, this.reading.id);
            if (reading != null) {
                this.reading = reading;
                readingProgress.setProgress(reading.chapter);
                number.setText(String.valueOf(reading.chapter));
            }
        }
    }

    public void retryLoading(View view) {
        loading.setVisibility(View.VISIBLE);
        retry.setVisibility(View.GONE);
        errorText.setText("");
        init(true);
    }

    public void refresh(MenuItem item) {
        retryLoading(null);
    }

    public void deleteManga(MenuItem item) {
        DatabaseHelper.reset(db, reading);
        Toast.makeText(this, getString(R.string.deleted), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chapter_menu, menu);
        menu.findItem(R.id.auto_refresh).setChecked(reading.autoRefresh);
        return super.onCreateOptionsMenu(menu);
    }

    public void toggleAutoRefresh(MenuItem item) {
        item.setChecked(!item.isChecked());

        if (reading.autoRefresh != item.isChecked()) {
            reading.autoRefresh = item.isChecked();
            ContentValues cv = new ContentValues();
            cv.put("auto_refresh", reading.autoRefresh ? 1 : 0);
            db.update("reading", cv, "reading_id IS ?", new String[]{String.valueOf(reading.id)});
        }
    }
}