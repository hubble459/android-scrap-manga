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

public class ChapterAdapter extends ArrayAdapter<Chapter> {
    private int read;
    protected int layout;
    private OnCheckedChanged listener;

    public ChapterAdapter(@NonNull Context context, @NonNull List<Chapter> chapters, int read) {
        super(context, 0, chapters);
        this.read = chapters.size() - read;
        this.layout = R.layout.adapter_item_chapter;
    }

    public void setOnCheckedChangeListener(OnCheckedChanged listener) {
        this.listener = listener;
    }

    public interface OnCheckedChanged {
        void changed();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(layout, parent, false);
        }

        Chapter chapter = getItem(position);

        TextView title = convertView.findViewById(R.id.title);
        title.setText(chapter.getTitle());

        TextView posted = convertView.findViewById(R.id.posted);
        posted.setText(Utils.Parse.toString(chapter.getPosted()));

        CheckBox downloaded = convertView.findViewById(R.id.downloaded);
        if (downloaded != null) {
            downloaded.setOnCheckedChangeListener((buttonView, isChecked) -> {
                chapter.setDownloaded(isChecked);
                if (listener != null) {
                    listener.changed();
                }
            });
            downloaded.setChecked(chapter.isDownloaded());
        } else {
            View check = convertView.findViewById(R.id.downloaded_check);
            check.setVisibility(chapter.isDownloaded() ? View.VISIBLE : View.INVISIBLE);
        }

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

    public void clearChecked() {
        for (int i = 0; i < getCount(); i++) {
            getItem(i).setDownloaded(false);
        }
        notifyDataSetChanged();
    }

    public void checkAll() {
        for (int i = 0; i < getCount(); i++) {
            getItem(i).setDownloaded(true);
        }
        notifyDataSetChanged();
    }
}
