package nl.hubble.scrapmanga.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ChapterAdapter;
import nl.hubble.scrapmanga.adapter.ChapterDownloadAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;
import nl.hubble.scrapmanga.util.FileUtil;
import nl.hubble.scrapmanga.view.LoadDialog;

public class ChapterListActivity extends CustomActivity implements AdapterView.OnItemClickListener {
    public static final String CHAPTER_KEY = "chapter";
    private Reading reading;
    private Manga manga;
    private SQLiteDatabase db;
    private ChapterAdapter adapter;
    private ListView list;
    private boolean downloading;
    private Select select;
    private MenuItem selectAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        manga = (Manga) getIntent().getSerializableExtra(MangaActivity.MANGA_KEY);
        reading = (Reading) getIntent().getSerializableExtra(MainActivity.READING_KEY);

        if (manga != null && reading != null) {
            list = findViewById(R.id.list);
            if (manga.getChapters().isEmpty()) {
                findViewById(R.id.no_chapters).setVisibility(View.VISIBLE);
            } else {
                String basePath = getExternalFilesDir(null).getAbsolutePath() + File.separator + "chapters" + File.separator;
                for (Chapter chapter : manga.getChapters()) {
                    chapter.setDownloaded(FileUtil.exists(basePath + chapter.getId()));
                }

                new Thread(() -> {
                    db = DatabaseHelper.getDatabase(this);

                    runOnUiThread(() -> init(downloading));
                }).start();
            }
        } else {
            // Show error?
            finish();
        }
    }

    private void init(boolean download) {
        int pos = list.getFirstVisiblePosition();
        int checked = getChecked();
        if (!download) {
            adapter = new ChapterAdapter(this, manga.getChapters(), reading.getChapter());
            list.setOnItemClickListener(this);
        } else {
            adapter = new ChapterDownloadAdapter(this, manga.getChapters(), reading.getChapter());
            adapter.setOnCheckedChangeListener(() -> {
                if (selectAll != null) {
                    int checked2 = getChecked();
                    Log.i("OWO", "init: " + checked2 + "/" + adapter.getCount());
                    if (checked2 == 0) {
                        selectAll.setIcon(R.drawable.ic_check_box_unchecked_24px);
                        select = Select.UNCHECKED;
                    } else if (checked2 == manga.getChapters().size()) {
                        selectAll.setIcon(R.drawable.ic_check_box_checked_24px);
                        select = Select.CHECKED;
                    } else {
                        selectAll.setIcon(R.drawable.ic_check_box_indeterminate_24px);
                        select = Select.INDETERMINATE;
                    }
                }
            });
            list.setOnItemClickListener((parent, view, position, id) -> {
                CheckBox checkbox = view.findViewById(R.id.downloaded);
                checkbox.setChecked(!checkbox.isChecked());
            });
        }

        select = checked > 0 ? checked == adapter.getCount() ? Select.CHECKED : Select.INDETERMINATE : Select.UNCHECKED;

        list.setAdapter(adapter);
        list.setSelection(pos);
    }

    private int getChecked() {
        int checked = 0;
        for (Chapter chapter : manga.getChapters()) {
            if (chapter.isDownloaded()) {
                checked++;
            }
        }
        return checked;
    }

    private void updateReading(int value) {
        if (reading.getChapter() != value) {
            reading.setChapter(value);
            DatabaseHelper.updateReading(db, reading);
        }
    }

    public void onDownload(MenuItem menuItem) {
        downloading = !downloading;
        selectAll.setVisible(downloading);

        if (!downloading) {
            // Download all selected manga
            ArrayList<Chapter> download = new ArrayList<>();
            ArrayList<Chapter> remove = new ArrayList<>();
            String basePath = this.getExternalFilesDir(null).getAbsolutePath() + File.separator + "chapters" + File.separator;
            for (Chapter chapter : manga.getChapters()) {
                String path = basePath + chapter.getId();

                boolean exists = FileUtil.exists(path);
                if (chapter.isDownloaded() && !exists) {
                    download.add(chapter);
                } else if (!chapter.isDownloaded() && exists) {
                    remove.add(chapter);
                }
            }
            int total = download.size() + remove.size();
            if (total > 0) {
                LoadDialog loadDialog = new LoadDialog(
                        this,
                        total,
                        dialog -> FileUtil.cancelDownloadChapter(),
                        errors -> {

                        }
                );

                for (Chapter chapter : remove) {
                    if (!FileUtil.remove(basePath + chapter.getId())) {
                        loadDialog.addError("Remove", "Failed to remove " + chapter.getTitle(), chapter);
                    }
                    loadDialog.increaseCount();
                }
                for (Chapter chapter : download) {
                    FileUtil.downloadChapter(this, basePath + chapter.getId(), chapter, new FileUtil.DownloadListener() {
                        @Override
                        public void onFinish() {
                            runOnUiThread(loadDialog::increaseCount);
                        }

                        @Override
                        public void onError(Exception e) {
                            runOnUiThread(() -> loadDialog.addError("Download", e.getMessage(), chapter));
                        }

                        @Override
                        public void progress(String file, int percent) {
                            runOnUiThread(() -> loadDialog.setCurrentTask("downloading " + file));
                        }

                        @Override
                        public void cancelled() {
                            runOnUiThread(loadDialog::increaseCount);
                        }
                    });
                }
            }
        }

        init(downloading);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        updateReading(reading.getTotalChapters() - position);

        Chapter chapter = (Chapter) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, ReadActivity.class);
        intent.putExtra(ChapterListActivity.CHAPTER_KEY, chapter);
        intent.putExtra(MangaActivity.MANGA_KEY, manga);
        intent.putExtra(MainActivity.READING_KEY, reading);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chapter_list, menu);
        selectAll = menu.findItem(R.id.select_all);
        selectAll.setOnMenuItemClickListener(item -> {
            switch (select) {
                case CHECKED:
                case INDETERMINATE:
                    adapter.clearChecked();
                    item.setIcon(R.drawable.ic_check_box_unchecked_24px);
                    select = Select.UNCHECKED;
                    break;
                case UNCHECKED:
                    adapter.checkAll();
                    item.setIcon(R.drawable.ic_check_box_checked_24px);
                    select = Select.CHECKED;
                    break;
            }
            return true;
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            Reading reading = DatabaseHelper.getReading(db, this.reading.getId());
            if (reading != null) {
                adapter.setRead(reading.getTotalChapters() - reading.getChapter());
                adapter.notifyDataSetChanged();
            }
        }
    }

    private enum Select {
        CHECKED,
        UNCHECKED,
        INDETERMINATE
    }
}