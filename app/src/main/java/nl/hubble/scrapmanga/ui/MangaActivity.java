package nl.hubble.scrapmanga.ui;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

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

import static nl.hubble.scrapmanga.util.DatabaseHelper.arrayAsString;
import static nl.hubble.scrapmanga.util.DatabaseHelper.filled;

public class MangaActivity extends CustomActivity implements LoadManga.OnFinishedListener {
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
        setContentView(R.layout.activity_manga);

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

            if (!DatabaseHelper.exists(db, reading.getHref())) {
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
        if (reading.getChapter() != value) {
            reading.setChapter(value);
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
        if ((reading.isAutoRefresh() && System.currentTimeMillis() - reading.getRefreshed() > refreshTime) || force) {
            try {
                new LoadManga(this, reading.getHref(), this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            reading = DatabaseHelper.getReading(db, reading.getId());
            manga = DatabaseHelper.getManga(this, db, reading);
            finished(manga);
        }
    }

    private boolean isComplete(Manga manga) {
        return filled(manga.getTitle()) && filled(manga.getHref()) && filled(manga.getHostname());
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
                if (manga == null) {
                    manga = DatabaseHelper.getManga(this, db, reading);
                }
                m.setId(manga.getId());
                manga = m;
                reading.setHref(m.getHref());
                reading.setTitle(m.getTitle());
                reading.setTotalChapters(m.getChapters().size());
                reading.setHostname(m.getHostname());
                reading.setRefreshed(System.currentTimeMillis());
                DatabaseHelper.updateManga(db, manga);
            }
            DatabaseHelper.updateReading(db, reading);

            // Title
            TextView title = findViewById(R.id.title);
            title.setText(manga.getTitle());
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setSubtitle(manga.getHostname());
            }

            // Cover
            if (filled(manga.getCover())) {
                ImageView cover = findViewById(R.id.cover);
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

            // Details
            MangaDetailView details = findViewById(R.id.details);
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

            // Description
            findViewById(R.id.desc_title).setVisibility(View.VISIBLE);
            TextView description = findViewById(R.id.description);
            description.setText(manga.getDescription());

            // Seekbar and Number
            TextView total = findViewById(R.id.total_chapters);
            total.setText(String.valueOf(reading.getTotalChapters()));
            number = findViewById(R.id.current_chapter);
            readingProgress = findViewById(R.id.current_chapter_progress);

            number.setText(String.valueOf(reading.getChapter()));
            readingProgress.setMax(reading.getTotalChapters());
            readingProgress.setProgress(reading.getChapter());

            findViewById(R.id.bottom_panel).setVisibility(View.VISIBLE);

            stopLoading();
        });
    }

    private void stopLoading() {
        loading.setVisibility(View.GONE);
        errorText.setText("");
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
        np.setMaxValue(reading.getTotalChapters());
        np.setValue(reading.getChapter());

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
        int chap = Math.min(reading.getTotalChapters() - 1, reading.getTotalChapters() - reading.getChapter());

        read(chap);
    }

    public void readFirst(View view) {
        read(reading.getTotalChapters() - 1);
    }

    public void readLast(View view) {
        if (reading.getChapter() < reading.getTotalChapters() - 1) {
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
        List<Chapter> chapters = manga.getChapters();
        if (!chapters.isEmpty()) {
            updateReading(reading.getTotalChapters() - chap);

            Chapter chapter = manga.getChapters().get(chap);
            Intent intent = new Intent(this, ReadActivity.class);
            intent.putExtra(ChapterListActivity.CHAPTER_KEY, chapter);
            intent.putExtra(MangaActivity.MANGA_KEY, manga);
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
            Reading reading = DatabaseHelper.getReading(db, this.reading.getId());
            if (reading != null) {
                this.reading = reading;
                readingProgress.setProgress(reading.getChapter());
                number.setText(String.valueOf(reading.getChapter()));
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
        getMenuInflater().inflate(R.menu.menu_manga, menu);
        menu.findItem(R.id.auto_refresh).setChecked(reading.isAutoRefresh());
        return super.onCreateOptionsMenu(menu);
    }

    public void toggleAutoRefresh(MenuItem item) {
        item.setChecked(!item.isChecked());

        if (reading.isAutoRefresh() != item.isChecked()) {
            reading.setAutoRefresh(item.isChecked());
            ContentValues cv = new ContentValues();
            cv.put("auto_refresh", reading.isAutoRefresh() ? 1 : 0);
            db.update("reading", cv, "reading_id IS ?", new String[]{String.valueOf(reading.getId())});
        }
    }

    public void openInBrowser(MenuItem item) {
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.cancel();
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(manga.getHref()));
                startActivity(browserIntent);
            } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText(manga.getTitle(), manga.getHref());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            }
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle("Website")
                .setMessage("Open in browser?")
                .setPositiveButton("Yes", listener)
                .setNegativeButton("No", listener)
                .setNeutralButton("Copy", listener)
                .show();
    }
}