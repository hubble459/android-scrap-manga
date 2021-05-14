package nl.hubble.scrapmanga.ui;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ImageAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.LoadChapter;
import nl.hubble.scrapmanga.util.ScrapUtils;
import nl.hubble.scrapmanga.view.PaginationView;

public class ReadActivity extends CustomActivity implements LoadChapter.OnFinishedListener, PaginationView.PaginationListener {
    protected Manga manga;
    protected Reading reading;
    protected Chapter chapter;
    protected List<String> images;
    protected PaginationView pv;
    private String referer;
    private ListView list;
    private ProgressBar loading;
    private boolean ignoreErrors;
    private boolean destroyed;
    private final ScrapUtils.ErrorListener errorListener = (e, url, imageView) -> {
        if (destroyed) return;

        DialogInterface.OnClickListener refresh = (dialog, which) -> {
            ignoreErrors = false;
            reload(chapter);
        };

        if (!ignoreErrors) {
            ignoreErrors = true;
            String error = null;
            if (e != null) {
                try {
                    error = e.getRootCauses().get(0).getMessage();
                } catch (Exception ex) {
                    error = e.getMessage();
                }
            }

            if (error == null || error.isEmpty()) {
                error = "N/A";
            }

            new MaterialAlertDialogBuilder(this)
                    .setTitle("Error")
                    .setMessage("Url = " + url + "\nError = " + error)
                    .setPositiveButton("Refresh", refresh)
                    .setNegativeButton("Continue Reading", (dialog, which) -> ignoreErrors = false)
                    .setOnDismissListener(dialog -> ignoreErrors = false)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_glide);

        chapter = (Chapter) getIntent().getSerializableExtra(ChapterListActivity.CHAPTER_KEY);
        manga = (Manga) getIntent().getSerializableExtra(ChapterActivity.MANGA_KEY);
        reading = (Reading) getIntent().getSerializableExtra(MainActivity.READING_KEY);
        pv = findViewById(R.id.pagination);
        loading = findViewById(R.id.loading);

        afterCreate();
    }

    protected void afterCreate() {
        if (chapter != null && manga != null) {
            list = findViewById(R.id.list);

            try {
                referer = new URL(manga.href).getHost();
            } catch (MalformedURLException e) {
                referer = manga.href;
            }

            reload(chapter);
        }
    }

    protected void clearList() {
        list.setAdapter(null);
    }

    protected void initList() {
        clearCache();

        ImageAdapter adapter = new ImageAdapter(this, images, referer, errorListener);
        list.setVisibility(View.VISIBLE);
        list.setAdapter(adapter);
        list.setOnItemLongClickListener((parent, view, position, id) -> {
            imageOptions(view.findViewById(R.id.image), (String) parent.getItemAtPosition(position));
            return true;
        });
    }

    private void clearCache() {
        Glide.get(this).clearMemory();
        new Thread(() -> Glide.get(this).clearDiskCache()).start();
    }

    private void imageOptions(ImageView image, String link) {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Options")
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{"Save", "Reload"}), (dialog, which) -> {
                    switch (which) {
                        case 0:
                            ScrapUtils.Image.saveImageFromView(this, image);
                            break;
                        case 1:
                            ScrapUtils.Image.loadImage(image, link, errorListener, referer, false);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.read_glide_menu, menu);
        return true;
    }

    @Override
    public void finished(List<String> images) {
        this.images = images;
        runOnUiThread(() -> {
            setTitle(chapter.title);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setSubtitle(Utils.Parse.toString(chapter.posted));
            }

            pv.init(this, manga, reading);

            loading(false);

            initList();
        });
    }

    private void loading(boolean flag) {
        loading.setVisibility(flag ? View.VISIBLE : View.GONE);
    }

    @Override
    public void error(Exception e) {
        runOnUiThread(() -> {
            setTitle(R.string.error);

            Toast.makeText(this, "error!\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

            loading(false);
        });
    }

    public void refresh(MenuItem item) {
        reload(chapter);
    }

    @Override
    public void reload(Chapter chapter) {
        setTitle(R.string.loading);
        clearList();
        loading(true);

        this.chapter = chapter;
        try {
            new LoadChapter(this, chapter.href, this);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        super.onDestroy();
    }
}