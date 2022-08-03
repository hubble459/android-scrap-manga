package nl.hubble.scrapmanga.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import nl.hubble.scrapmanga.R;
import nl.hubble.scrapmanga.util.ImageUtil;

public class ImageAdapter extends ArrayAdapter<String> {
    private final String referer;
    private final ImageUtil.ErrorListener errorListener;
    private final boolean local;

    public ImageAdapter(@NonNull Context context, @NonNull List<String> reading, String referer, boolean local, ImageUtil.ErrorListener errorListener) {
        super(context, 0, reading);
        this.referer = referer;
        this.local = local;
        this.errorListener = errorListener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_image, parent, false);
        }

        ImageView imageView = convertView.findViewById(R.id.image);

        String urlString = getItem(position);

        ImageUtil.loadImage(imageView, urlString, errorListener, referer, local);

        return convertView;
    }
}
