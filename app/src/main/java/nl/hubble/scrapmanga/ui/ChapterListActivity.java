package nl.hubble.scrapmanga.ui;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.adapter.ChapterAdapter;
import nl.hubble.scrapmanga.model.CustomActivity;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;

public class ChapterListActivity extends CustomActivity implements AdapterView.OnItemClickListener {
    public static final String CHAPTER_KEY = "chapter";
    private Reading reading;
    private Manga manga;
    private SQLiteDatabase db;
    private ChapterAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chapter_list);

        manga = (Manga) getIntent().getSerializableExtra(ChapterActivity.MANGA_KEY);
        reading = (Reading) getIntent().getSerializableExtra(MainActivity.READING_KEY);

        if (manga != null && reading != null) {
            if (manga.chapters.isEmpty()) {
                findViewById(R.id.no_chapters).setVisibility(View.VISIBLE);
            } else {
                new Thread(() -> {
                    db = DatabaseHelper.getDatabase(this);

                    runOnUiThread(this::init);
                }).start();
            }
        } else {
            // Show error?
            finish();
        }
    }

    private void init() {
        adapter = new ChapterAdapter(this, manga.chapters, reading.chapter);

        ListView list = findViewById(R.id.list);
        list.setAdapter(adapter);

        list.setOnItemClickListener(this);
    }

    private void updateReading(int value) {
        if (reading.chapter != value) {
            reading.chapter = value;
            DatabaseHelper.updateReading(db, reading);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        updateReading(reading.totalChapters - position);

        Chapter chapter = (Chapter) parent.getItemAtPosition(position);
        Intent intent = new Intent(this, ReadActivity.class);
        intent.putExtra(ChapterListActivity.CHAPTER_KEY, chapter);
        intent.putExtra(ChapterActivity.MANGA_KEY, manga);
        intent.putExtra(MainActivity.READING_KEY, reading);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            Reading reading = DatabaseHelper.getReading(db, this.reading.id);
            if (reading != null) {
                adapter.setRead(reading.totalChapters - reading.chapter);
                adapter.notifyDataSetChanged();
            }
        }
    }
}