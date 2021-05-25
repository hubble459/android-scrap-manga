package nl.hubble.scrapmanga.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import nl.hubble.scrapmanga.R;

/**
 * Show manga details like status, authors and genres.
 */
public class MangaDetailView extends TableLayout {
    private TableRow status, authors, alts, genres, interval, updated, chapters;
    private TextView statusTV, authorsTV, altsTV, genresTV, intervalTV, updatedTV, chaptersTV;

    public MangaDetailView(Context context) {
        super(context);
        init();
    }

    public MangaDetailView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.view_manga_detail, this);

        status = view.findViewById(R.id.status);
        authors = view.findViewById(R.id.authors);
        alts = view.findViewById(R.id.alts);
        genres = view.findViewById(R.id.genres);
        interval = view.findViewById(R.id.interval);
        updated = view.findViewById(R.id.updated);
        chapters = view.findViewById(R.id.chapters);

        statusTV = view.findViewById(R.id.status_text);
        authorsTV = view.findViewById(R.id.authors_text);
        altsTV = view.findViewById(R.id.alts_text);
        genresTV = view.findViewById(R.id.genres_text);
        intervalTV = view.findViewById(R.id.interval_text);
        updatedTV = view.findViewById(R.id.updated_text);
        chaptersTV = view.findViewById(R.id.chapters_text);
    }

    public void setStatus(String text) {
        if (!text.isEmpty()) {
            statusTV.setText(text);
            status.setVisibility(VISIBLE);
        } else {
            status.setVisibility(GONE);
        }
    }

    public void setAuthors(String text) {
        if (!text.isEmpty()) {
            authorsTV.setText(text);
            authors.setVisibility(VISIBLE);
        } else {
            authors.setVisibility(GONE);
        }
    }

    public void setAltTitles(String text) {
        if (!text.isEmpty()) {
            altsTV.setText(text);
            alts.setVisibility(VISIBLE);
        } else {
            alts.setVisibility(GONE);
        }
    }

    public void setGenres(String text) {
        if (!text.isEmpty()) {
            genresTV.setText(text);
            genres.setVisibility(VISIBLE);
        } else {
            genres.setVisibility(GONE);
        }
    }

    public void setInterval(String text) {
        if (!text.isEmpty()) {
            intervalTV.setText(text);
            interval.setVisibility(VISIBLE);
        } else {
            interval.setVisibility(GONE);
        }
    }

    public void setUpdated(String text) {
        if (!text.isEmpty()) {
            updatedTV.setText(text);
            updated.setVisibility(VISIBLE);
        } else {
            updated.setVisibility(GONE);
        }
    }

    public void setChapters(int number) {
        chaptersTV.setText(String.valueOf(number));
        chapters.setVisibility(VISIBLE);
    }
}