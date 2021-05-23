package nl.hubble.scraper.type;

import android.content.Context;

import java.net.URL;

import nl.hubble.scraper.model.Manga;

public class MangaKakalot extends MangaNelo {
    public MangaKakalot(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (accepts(manga.getHostname())) { // host could be manganelo
            if (!manga.getAuthors().isEmpty()) {
                manga.getAuthors().remove(0);
            }
            if (!url.getHost().contains("mangakakalots") && !manga.getAltTitles().isEmpty()) {
                manga.getAltTitles().remove(0);
            }
        }
        return manga;
    }

    @Override
    public String[] hostnames() {
        return new String[]{"mangakakalot"};
    }
}
