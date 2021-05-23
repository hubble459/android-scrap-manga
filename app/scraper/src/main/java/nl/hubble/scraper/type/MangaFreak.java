package nl.hubble.scraper.type;

import android.content.Context;

import java.util.List;

public class MangaFreak extends MangaStream {
    public MangaFreak(Context context) {
        super(context);
    }

    @Override
    protected List<String> altTitles(String title) {
        List<String> list = super.altTitles(title);
        list.set(0, list.get(0).replace("Alternative:", ""));
        list.set(0, list.get(0).replace("Alternative : ", ""));
        return list;
    }

    @Override
    protected List<String> authors() {
        List<String> list = super.authors();
        list.set(0, list.get(0).replace("Author:", ""));
        list.set(0, list.get(0).replace("Author(s) : ", ""));
        return list;
    }

    @Override
    protected List<String> genres() {
        List<String> list = super.genres();
        list.remove(0);
        return list;
    }

    @Override
    public String[] hostnames() {
        return new String[]{"mangafreak.cloud", "mangarockteam.site"};
    }
}
