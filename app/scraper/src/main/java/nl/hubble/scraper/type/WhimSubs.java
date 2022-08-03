package nl.hubble.scraper.type;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nl.hubble.scraper.model.Manga;
import nl.hubble.scraper.util.Utils;

public class WhimSubs extends QueryScraper {
    public WhimSubs(Context context) {
        super(context);
    }

    @Override
    public String[] hostnames() {
        return new String[]{"whimsubs.xyz"};
    }

    @Override
    public Manga parse(URL url, int timeout) throws Exception {
        Manga m = super.parse(url, timeout);
        m.setTitle(m.getTitle().replace("rss_feed", ""));
        return m;
    }

    @Override
    public List<String> images(URL url, int timeout) throws Exception {
        url = new URL(url.toString() + "/manifest.json");
        JSONObject object = Utils.Parse.getJSON(url, timeout);
        JSONArray array = object.getJSONArray("readingOrder");
        List<String> images = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject o = array.getJSONObject(i);
            images.add(o.getString("href"));
        }
        return images;
    }
}
