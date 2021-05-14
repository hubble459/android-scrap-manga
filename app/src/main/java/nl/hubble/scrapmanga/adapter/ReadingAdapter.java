package nl.hubble.scrapmanga.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Locale;

import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.filter.ReadingFilter;
import nl.hubble.scrapmanga.model.Reading;

public class ReadingAdapter extends ArrayAdapter<Reading> {
    private final ReadingFilter filter;

    public ReadingAdapter(@NonNull Context context, @NonNull List<Reading> reading) {
        super(context, 0, reading);
        filter = new ReadingFilter(reading, this);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_reading, parent, false);
        }

        Reading reading = getItem(position);

        TextView title = convertView.findViewById(R.id.title);
        title.setText(reading.title);

        TextView hostname = convertView.findViewById(R.id.hostname);
        hostname.setText(reading.hostname);

        TextView unread = convertView.findViewById(R.id.unread);
        int diff = reading.totalChapters - reading.chapter;
        if (diff > 0) {
            unread.setText(String.format(Locale.UK, getContext().getString(R.string.unread_), diff));
            convertView.setBackgroundColor(Color.TRANSPARENT);
        } else {
            unread.setText("");
            convertView.setBackgroundColor(Color.parseColor("#3a98e698"));
        }

        ProgressBar pb = convertView.findViewById(R.id.progress);
        pb.setMax(reading.totalChapters);
        pb.setProgress(reading.chapter);

        return convertView;
    }

    public void notifyFilter(List<Reading> readings) {
        filter.notifyDataSetChanged(readings);
    }

    @NonNull
    @Override
    public ReadingFilter getFilter() {
        return filter;
    }

    public List<Reading> getList() {
        return filter.getList();
    }
}
