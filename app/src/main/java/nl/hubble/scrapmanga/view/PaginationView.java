package nl.hubble.scrapmanga.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.model.Reading;
import nl.hubble.scrapmanga.util.DatabaseHelper;

public class PaginationView extends LinearLayout {
    private Button prev, next;
    private Spinner chapterSpinner;
    private Reading reading;
    private Manga manga;
    private PaginationListener listener;

    public PaginationView(Context context) {
        super(context);
        init();
    }

    public PaginationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_pagination, this);

        prev = view.findViewById(R.id.previous);
        next = view.findViewById(R.id.next);
        chapterSpinner = view.findViewById(R.id.chapter_spinner);

        next.setOnClickListener(v -> {
            if (reading == null || manga == null || listener == null) return;

            int chapter_ = Math.min(reading.getChapter() + 1, reading.getTotalChapters());
            int chap = reading.getTotalChapters() - chapter_;
            chapterSpinner.setSelection(chap);
        });

        prev.setOnClickListener(v -> {
            if (reading == null || manga == null || listener == null) return;

            int chapter_ = Math.max(reading.getChapter() - 1, 1);
            int chap = reading.getTotalChapters() - chapter_;
            chapterSpinner.setSelection(chap);
        });

        chapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (reading == null || manga == null || listener == null) return;

                int chap = reading.getTotalChapters() - position;
                listener.mightReload();
                if (chap != reading.getChapter()) {
                    reading.setChapter(chap);
                    DatabaseHelper.updateReading(getContext(), reading);
                    Chapter chapter = manga.getChapters().get(position);
                    listener.reload(chapter);

                    refreshEnabled();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    public void init(PaginationListener listener, Manga manga, Reading reading) {
        if (this.listener == null) {
            List<String> chapters = new ArrayList<>();
            for (Chapter chapter : manga.getChapters()) {
                chapters.add(chapter.getTitle());
            }
            chapterSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, chapters));
            chapterSpinner.setSelection(reading.getTotalChapters() - reading.getChapter());
        }

        this.reading = reading;
        this.manga = manga;
        this.listener = listener;

        refreshEnabled();
    }

    private void refreshEnabled() {
        if (reading != null) {
            prev.setEnabled(reading.getChapter() != 1);
            next.setEnabled(reading.getChapter() != reading.getTotalChapters());
        }
    }

    public interface PaginationListener {
        void reload(Chapter chapter);

        void mightReload();
    }
}