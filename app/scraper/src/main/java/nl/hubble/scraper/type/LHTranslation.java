package nl.hubble.scraper.type;

import android.content.Context;

import java.net.URL;

import nl.hubble.scraper.model.Manga;

public class LHTranslation extends Query {
    private final String[] accepts = new String[]{"lhtranslation"};

    public LHTranslation(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (!manga.authors.isEmpty()) {
            manga.authors.set(0, manga.authors.get(0).replace("Author(s): ", ""));
        }
        if (!manga.altTitles.isEmpty()) {
            manga.altTitles.set(0, manga.altTitles.get(0).replace("Other names: ", ""));
        }
        return manga;
    }

    @Override
    public boolean accepts(URL url) {
        String hostname = url.getHost();
        for (String accept : accepts) {
            if (hostname.contains(accept)) {
                return true;
            }
        }
        return false;
    }
}
