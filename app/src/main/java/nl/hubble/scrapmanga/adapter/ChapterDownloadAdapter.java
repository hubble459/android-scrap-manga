package nl.hubble.scrapmanga.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scrapmanga.R;

public class ChapterDownloadAdapter extends ChapterAdapter {
    public ChapterDownloadAdapter(@NonNull Context context, @NonNull List<Chapter> chapters, int read) {
        super(context, chapters, read);
        this.layout = R.layout.adapter_item_chapter_download;
    }
}
