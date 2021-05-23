package nl.hubble.scrapmanga.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;

import nl.hubble.scrapmanga.R;

public class ImageUtil {
    public static void saveImageFromView(Context context, final ImageView image) {
        final String imageTitle = "image_" + image.getId();

        Drawable drawable = image.getDrawable();
        if (!(drawable instanceof BitmapDrawable)) return;
        Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
        String imagePath = MediaStore.Images.Media.insertImage(
                context.getContentResolver(),
                bitmap,
                imageTitle,
                imageTitle
        );
        Uri.parse(imagePath);
        Toast.makeText(context, "Image Saved", Toast.LENGTH_SHORT).show();
    }

    public static void loadImage(ImageView image, String urlString, ErrorListener errorListener, String referer, boolean local) {
        GlideUrl url = null;

        RequestBuilder<Drawable> rb;
        if (local) {
            rb = Glide.with(image).load(new File(urlString));
        } else {
            if (urlString.contains("isekaiscan") || urlString.contains("zeroscans") || urlString.contains("the-nonames")) {
                url = new GlideUrl(urlString);
            } else {
                url = new GlideUrl(urlString, new LazyHeaders.Builder()
                        .addHeader("referer", referer)
                        .build());
            }
            rb = Glide.with(image).load(url);
        }

        rb
                .listener(createRequestListener(errorListener, local ? urlString : url.toStringUrl(), image))
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.error)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .dontTransform()
                .encodeQuality(100)
                .into(image);
    }

    private static RequestListener<Drawable> createRequestListener(ErrorListener errorListener, String url, ImageView image) {
        return new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                errorListener.error(e, url, image);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                return false;
            }
        };
    }

    public interface ErrorListener {
        void error(GlideException e, String url, ImageView imageView);
    }
}