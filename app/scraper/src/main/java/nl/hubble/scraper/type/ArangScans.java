package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ArangScans extends QueryScraper {
    public ArangScans(Context context) {
        super(context);
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        Document doc = Jsoup.parse(url, timeout);

        List<String> images = new ArrayList<>();
        Element script = doc.selectFirst("#chapter_preloaded_images");
        String data = script.data().trim();
        data = data.substring("var chapter_preloaded_images = ".length());
        JSONArray array = new JSONArray(data);
        for (int i = 0; i < array.length(); i++) {
            images.add(array.optString(i));
        }

        return images;
    }

    @Override
    public String[] hostnames() {
        return new String[]{"arangscans"};
    }
}
