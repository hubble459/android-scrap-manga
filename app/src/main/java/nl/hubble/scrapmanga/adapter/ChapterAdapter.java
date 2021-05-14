package nl.hubble.scrapmanga.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nl.hubble.scraper.model.Chapter;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;

public class ChapterAdapter extends ArrayAdapter<Chapter> {
    private int read;

    public ChapterAdapter(@NonNull Context context, @NonNull List<Chapter> chapters, int read) {
        super(context, 0, chapters);
        this.read = chapters.size() - read;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_chapter, parent, false);
        }

        Chapter chapter = getItem(position);

        TextView title = convertView.findViewById(R.id.title);
        title.setText(chapter.title);

        TextView posted = convertView.findViewById(R.id.posted);
        posted.setText(Utils.Parse.toString(chapter.posted));

        if (position >= read) {
            convertView.setBackgroundColor(Color.parseColor("#69696969"));
        } else {
            convertView.setBackgroundColor(Color.parseColor("#000000FF"));
        }

        return convertView;
    }

    public void setRead(int read) {
        this.read = read;
    }
}
