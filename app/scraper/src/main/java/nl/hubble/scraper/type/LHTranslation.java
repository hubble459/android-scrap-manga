package nl.hubble.scraper.type;

import android.content.Context;

import java.net.URL;

import nl.hubble.scraper.model.Manga;

public class LHTranslation extends QueryScraper {
    public LHTranslation(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (!manga.getAuthors().isEmpty()) {
            manga.getAuthors().set(0, manga.getAuthors().get(0).replace("Author(s): ", ""));
        }
        if (!manga.getAltTitles().isEmpty()) {
            manga.getAltTitles().set(0, manga.getAltTitles().get(0).replace("Other names: ", ""));
        }
        return manga;
    }

    @Override
    public String[] hostnames() {
        return new String[]{"lhtranslation"};
    }
}
