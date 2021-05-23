package nl.hubble.scrapmanga.ui;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ImageAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.FileUtil;
import nl.hubble.scrapmanga.util.ImageUtil;
import nl.hubble.scrapmanga.util.LoadChapter;
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
    private final ImageUtil.ErrorListener errorListener = (e, url, imageView) -> {
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
                    .setTitle(R.string.error)
                    .setMessage("Url = " + url + "\nError = " + error)
                    .setPositiveButton(R.string.refresh, refresh)
                    .setNegativeButton(R.string.continue_reading, (dialog, which) -> ignoreErrors = false)
                    .setOnDismissListener(dialog -> ignoreErrors = false)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read);

        chapter = (Chapter) getIntent().getSerializableExtra(ChapterListActivity.CHAPTER_KEY);
        manga = (Manga) getIntent().getSerializableExtra(MangaActivity.MANGA_KEY);
        reading = (Reading) getIntent().getSerializableExtra(MainActivity.READING_KEY);
        pv = findViewById(R.id.pagination);
        loading = findViewById(R.id.loading);

        afterCreate();
    }

    protected void afterCreate() {
        if (chapter != null && manga != null) {
            list = findViewById(R.id.list);

            try {
                referer = new URL(manga.getHref()).getHost();
            } catch (MalformedURLException e) {
                referer = manga.getHref();
            }

            reload(chapter);
        }
    }

    protected void clearList() {
        list.setAdapter(null);
    }

    protected void initList() {
        clearCache();

        ImageAdapter adapter = new ImageAdapter(this, images, referer, chapter.isDownloaded(), errorListener);
        list.setVisibility(View.VISIBLE);
        list.setAdapter(adapter);
        list.setSelection(chapter.getPage());
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
                .setTitle(R.string.options)
                .setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, new String[]{getString(R.string.image_save), getString(R.string.image_reload)}), (dialog, which) -> {
                    switch (which) {
                        case 0:
                            if (chapter.isDownloaded()) {
                                if (ContextCompat.checkSelfPermission(ReadActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    ActivityCompat.requestPermissions(ReadActivity.this,
                                            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                            0);
                                } else {
                                    String extension = link.substring(link.lastIndexOf("."));
                                    FileUtil.copy(link, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath() + File.separator + image.getId() + extension, new FileUtil.DownloadAdapter() {
                                        @Override
                                        public void onFinish() {
                                            runOnUiThread(() -> Toast.makeText(ReadActivity.this, ReadActivity.this.getString(R.string.image_saved), Toast.LENGTH_SHORT).show());
                                        }

                                        @Override
                                        public void onError(Exception e) {
                                            runOnUiThread(() -> Toast.makeText(ReadActivity.this, e.getMessage(), Toast.LENGTH_LONG).show());
                                        }
                                    });
                                }
                            } else {
                                ImageUtil.saveImageFromView(this, image);
                            }
                            break;
                        case 1:
                            ImageUtil.loadImage(image, link, errorListener, referer, chapter.isDownloaded());
                            break;
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_read, menu);
        return true;
    }

    @Override
    public void finished(List<String> images) {
        this.images = images;
        runOnUiThread(() -> {
            setTitle(chapter.getTitle());
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setSubtitle(Utils.Parse.toString(chapter.getPosted()));
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
        if (chapter.isDownloaded()) {
            String basePath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "chapters" + File.separator + chapter.getId() + File.separator;
            ArrayList<String> images = new ArrayList<>();
            File[] files = new File(basePath).listFiles();
            if (files != null) {
                Arrays.sort(files, (o1, o2) -> {
                    String s1 = o1.getName().split("\\.")[0];
                    String s2 = o2.getName().split("\\.")[0];
                    int n1 = Integer.parseInt(s1);
                    int n2 = Integer.parseInt(s2);
                    return Integer.compare(n1, n2);
                });
                for (File file : files) {
                    images.add(file.getAbsolutePath());
                }
                finished(images);
            }
        } else {
            try {
                new LoadChapter(this, new URL(chapter.getHref()), this);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void mightReload() {
        updatePage();
    }

    @Override
    protected void onDestroy() {
        destroyed = true;
        updatePage();
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        updatePage();
        super.onPause();
    }

    private void updatePage() {
        if (chapter != null && list != null) {
            new Thread(() -> DatabaseHelper.updatePage(this, chapter, list.getFirstVisiblePosition())).start();
        }
    }

    public void openInBrowser(MenuItem item) {
        DialogInterface.OnClickListener listener = (dialog, which) -> {
            if (which == DialogInterface.BUTTON_NEGATIVE) {
                dialog.cancel();
            } else if (which == DialogInterface.BUTTON_POSITIVE) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(chapter.getHref()));
                startActivity(browserIntent);
            } else if (which == DialogInterface.BUTTON_NEUTRAL) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText(chapter.getTitle(), chapter.getHref());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(this, getString(R.string.copied_to_clipboard), Toast.LENGTH_SHORT).show();
                }
            }
        };

        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.website)
                .setMessage(R.string.open_in_browser)
                .setPositiveButton(R.string.yes, listener)
                .setNegativeButton(R.string.no, listener)
                .setNeutralButton(R.string.copy, listener)
                .show();
    }
}