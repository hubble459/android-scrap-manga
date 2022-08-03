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

public class ZeroScans extends QueryScraper {
    public ZeroScans(Context context) {
        super(context);
    }

    @Override
    public String[] hostnames() {
        return new String[]{"the-nonames.com", "reaperscans.com", "zeroscans.com"};
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga manga = super.parse(url, timeout);
        parseCover(url, manga);
        return manga;
    }

    @Override
    public List<Manga> search(String hostname, String query, int timeout) throws Exception {
        List<Manga> results = super.search(hostname, query, timeout);
        for (Manga manga : results) {
            parseCover(new URL(searchHrefQuery), manga);
        }
        return results;
    }

    private void parseCover(URL url, Manga manga) {
        if (!manga.getCover().isEmpty()) {
            manga.setCover(manga.getCover().substring(21).replace(")", ""));
            if (!manga.getCover().contains(url.getHost())) {
                manga.setCover(url.getProtocol() + "://" + url.getHost() + manga.getCover());
            }
        }
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
}
