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

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;

import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scrapmanga.R;

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

        String hostname = manga.getHostname();
        ImageView image = convertView.findViewById(R.id.image);
        Glide
                .with(image)
                .load(hostname.contains("zeroscans") ? manga.getCover() : new GlideUrl(manga.getCover(), new LazyHeaders.Builder().addHeader("referer", hostname).build()))
                .into(image);

        return convertView;
    }
}
