package nl.hubble.scrapmanga.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;
import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.util.ImageUtil;

public class SearchAdapter extends ArrayAdapter<Manga> {
    public SearchAdapter(@NonNull Context context, @NonNull List<Manga> manga) {
        super(context, 0, manga);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_search_manga, parent, false);
        }

        Manga manga = getItem(position);

        TextView title = convertView.findViewById(R.id.title);
        title.setText(manga.getTitle());

        TextView hostname = convertView.findViewById(R.id.hostname);
        hostname.setText(manga.getHostname());

        TextView updated = convertView.findViewById(R.id.updated);
        if (manga.getUpdated() > 0) {
            updated.setText(Utils.Parse.toTimeString(manga.getUpdated()));
        } else {
            updated.setText("");
        }

        ImageView image = convertView.findViewById(R.id.image);
        String cover = manga.getCover();
        image.setVisibility(cover == null ? View.GONE : View.VISIBLE);
        if (cover != null && !cover.isEmpty()) {
            ImageUtil.loadImage(image, cover, null, manga.getHostname(), false);
        }
        return convertView;
    }
}
