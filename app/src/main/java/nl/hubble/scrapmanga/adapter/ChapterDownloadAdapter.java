package nl.hubble.scrapmanga.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;

public class ChapterDownloadAdapter extends ChapterAdapter {
    public ChapterDownloadAdapter(@NonNull Context context, @NonNull List<Chapter> chapters, int read) {
        super(context, chapters, read);
        this.layout = R.layout.adapter_item_chapter_download;
    }
}
