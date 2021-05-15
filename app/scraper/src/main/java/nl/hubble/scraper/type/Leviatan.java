package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Manga;

public class Leviatan extends Query {
    private final String[] accepts = new String[]{"leviatan", "the-nonames", "reaperscans", "zeroscans"};

    public Leviatan(Context context) {
        super(context);
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        if (!manga.getCover().isEmpty()) {
            manga.setCover(manga.getCover().substring(21).replace(")", ""));
            if (!manga.getCover().contains(url.getHost())) {
                manga.setCover(url.getProtocol() + "://" + url.getHost() + manga.getCover());
            }
        }

        return manga;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        Document doc = Jsoup.parse(url, timeout);

        List<String> images = new ArrayList<>();
        Element script = doc.selectFirst("#pages-container + *");
        String data = script.data().trim();
        data = data.split("window\\.chapterPages = ", 2)[1];
        data = data.split(";window\\.nextChapter", 2)[0];
        JSONArray array = new JSONArray(data);
        for (int i = 0; i < array.length(); i++) {
            String path = array.optString(i);
            if (!path.isEmpty()) {
                if (!path.contains(url.getHost())) {
                    path = url.getProtocol() + "://" + url.getHost() + path;
                }

                images.add(path);
            }
        }

        return images;
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
