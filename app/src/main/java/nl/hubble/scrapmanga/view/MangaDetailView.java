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
    private TableRow status, authors, alts, genres, updated;
    private TextView statusTV, authorsTV, altsTV, genresTV, updatedTV;

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
        updated = view.findViewById(R.id.updated);

        statusTV = view.findViewById(R.id.status_text);
        authorsTV = view.findViewById(R.id.authors_text);
        altsTV = view.findViewById(R.id.alts_text);
        genresTV = view.findViewById(R.id.genres_text);
        updatedTV = view.findViewById(R.id.updated_text);
    }

    public void setStatus(String text) {
        if (!text.isEmpty()) {
            statusTV.setText(text);
            status.setVisibility(VISIBLE);
        }
    }

    public void setAuthors(String text) {
        if (!text.isEmpty()) {
            authorsTV.setText(text);
            authors.setVisibility(VISIBLE);
        }
    }

    public void setAltTitles(String text) {
        if (!text.isEmpty()) {
            altsTV.setText(text);
            alts.setVisibility(VISIBLE);
        }
    }

    public void setGenres(String text) {
        if (!text.isEmpty()) {
            genresTV.setText(text);
            genres.setVisibility(VISIBLE);
        }
    }

    public void setUpdated(String text) {
        if (!text.isEmpty()) {
            updatedTV.setText(text);
            updated.setVisibility(VISIBLE);
        }
    }
}