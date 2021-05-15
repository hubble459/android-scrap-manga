package nl.hubble.scraper.type;

import android.content.Context;

import java.net.URL;

import nl.hubble.scraper.model.Manga;

public class MangaKakalot extends Query {
    private final String[] accepts = new String[]{"mangakakalot"};

    public MangaKakalot(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (accepts(manga.getHostname())) {
            if (!manga.getAuthors().isEmpty()) {
                manga.getAuthors().remove(0);
            }
            if (!manga.getAltTitles().isEmpty()) {
                manga.getAltTitles().remove(0);
            }
        }
        return manga;
    }

    @Override
    public boolean accepts(URL url) {
        String hostname = url.getHost();
        return accepts(hostname);
    }

    private boolean accepts(String hostname) {
        for (String accept : accepts) {
            if (hostname.contains(accept)) {
                return true;
            }
        }
        return false;
    }
}
